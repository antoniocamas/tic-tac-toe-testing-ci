properties([
    parameters([
	string(defaultValue: '',
	       description: 'Next Version after the release',
	       name: 'nextVersion'),
    ])
])

if (params.nextVersion == '') {
    echo 'Build Failed: nextVersion parameter is mandatory'
    currentBuild.result = 'FAILURE'
    return
}
echo "nextVersion: ${params.nextVersion}"

node {
    try {
	def tagVersion = ''
	stage('Preparation') {
	    //The Git SCM plugin has a known bug/limitation to push tags of commit to git
	    // https://issues.jenkins-ci.org/browse/JENKINS-28335?focusedCommentId=269000&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-269000
	    // One of the proposed workarounds is to install and use the plugin sshagent
	    // The problem this workaround is that we have to clone and configure the repository in the job
	    // this is a maintenance problem.
	    sshagent(['hudson_gerrit']) {
		sh '''git clone ssh://hudson@10.0.2.15:29418/tic-tac-toe-testing.git .
                      gitdir=$(git rev-parse --git-dir)
                      scp -p -P 29418 hudson@10.0.2.15:hooks/commit-msg ${gitdir}/hooks/
                      git config user.name "hudson"
                      git config user.email hudson@example.com
                    '''
	    }
	}

	stage('Remove Version Suffix') {
	    sh '''docker run --rm \
               -v codeurjc-forge-jenkins-volume:/app \
               -w /app/workspace/\$JOB_NAME/continous_integration/pom_version_updater/ \
               atonich/python3-lxml \
               sh -c "python3 pom-version-updater.py -c -i ../../pom.xml"'''
	    
	    def getVersion = '''docker run --rm \
               -v codeurjc-forge-jenkins-volume:/app \
               -w /app/workspace/\$JOB_NAME/continous_integration/pom_version_updater/ \
               atonich/python3-lxml \
               sh -c "python3 pom-version-updater.py -r ../../pom.xml"'''
	    
	    tagVersion = sh(returnStdout: true, script: getVersion).trim()
	    echo "Releasing Version: ${tagVersion}"
	}
	stage('Build') {
	        sh '''docker run --rm \
                -v $HOME/.m2:/root/.m2 \
                -v codeurjc-forge-jenkins-volume:/src \
                -w /src/workspace/\$JOB_NAME \
                maven:3.6.1-jdk-8 \
                /bin/bash -c "mvn package -DskipTests"'''
	}

	try {
	    stage('Test') {
		// Run the build
		sh '''docker run --rm \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        -v $HOME/.m2:/root/.m2 \
                        -v codeurjc-forge-jenkins-volume:/src \
                        -w /src/workspace/\$JOB_NAME \
                        -p 7070:7070 \
                        maven:3.6.1-jdk-8 \
                        /bin/bash -c "mvn test -DAPP_URL=http://10.0.2.15:7070/"'''
	    }
	}
	finally {
	    stage('Result'){
		archiveArtifacts 'target/*.flv'
		junit 'target/**/*.xml'
	    }
	}
	stage('Publish Release Image') {
	    env.tagVersion = tagVersion
	    sh ''' mv target/*.jar continous_integration/build/'''
	    sh ''' cd continous_integration/build && ./build-image-with-tag.sh "$tagVersion"'''
	    sh '''docker tag atonich/tic-tac-toe-testing:"$tagVersion" atonich/tic-tac-toe-testing:latest'''
	    withDockerRegistry(credentialsId: 'atonich_dockerhub') {
		sh ''' docker push atonich/tic-tac-toe-testing:"$tagVersion"'''
		sh ''' docker push atonich/tic-tac-toe-testing:latest'''
	    }
	}
	stage('Tag Git commit')
	{
	    env.tagVersion = tagVersion
	    sshagent(['hudson_gerrit']) {
		sh '''
                     git tag -am "$tagVersion" "$tagVersion"
                     git push --tags
                   '''
	    }
	}
	stage('Create New Version')
	{
	    env.nextVersion = params.nextVersion
	    sh '''docker run --rm \
                   -v codeurjc-forge-jenkins-volume:/app \
                   -w /app/workspace/\$JOB_NAME/continous_integration/pom_version_updater/ \
                   atonich/python3-lxml \
                   sh -c "python3 pom-version-updater.py -v $nextVersion -i ../../pom.xml"
               '''
	    sshagent(['hudson_gerrit']) {
		sh ''' git add pom.xml
	               git commit -m "Update to version $nextVersion"
                       git push origin HEAD:refs/for/master
                       ssh -p 29418 hudson@10.0.2.15 gerrit review \
                           --code-review +2 --submit $(git rev-parse HEAD)
                   '''
	    }
	}
    }
    catch (exc){
	echo 'Build Failed'
	currentBuild.result = 'FAILURE'
    }
    finally {
	//clean up
	cleanWs()
    }
}
