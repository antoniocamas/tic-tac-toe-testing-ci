node {
    def tag = ''
    stage('Preparation') {

	final scmVars = checkout(
	    [$class: 'GitSCM', branches: [[name: '*/master']],
	     doGenerateSubmoduleConfigurations: false,
	     extensions: [], submoduleCfg: [],
	     userRemoteConfigs: [
		    [credentialsId: 'hudson_gerrit',
		     url: 'ssh://10.0.2.15:29418/tic-tac-toe-testing']]]
	)
	echo "scmVars: ${scmVars}"
	echo "scmVars.GIT_COMMIT: ${scmVars.GIT_COMMIT}"
	echo "scmVars.GIT_BRANCH: ${scmVars.GIT_BRANCH}"
    }
    try {
	stage('Build') {
	    sh '''docker run --rm \
                -v $HOME/.m2:/root/.m2 \
                -v codeurjc-forge-jenkins-volume:/src \
                -w /src/workspace/\$JOB_NAME \
                maven:3.6.1-jdk-8 \
                    /bin/bash -c "mvn package -DskipTests"'''
	}
	stage('Repository Test') {
	    sh '''docker run --rm \
                  -v /var/run/docker.sock:/var/run/docker.sock \
                  -v $HOME/.m2:/root/.m2 \
                  -v codeurjc-forge-jenkins-volume:/src \
                  -w /src/workspace/\$JOB_NAME \
                  -p 7070:7070 \
                  maven:3.6.1-jdk-8 \
                  /bin/bash -c "mvn test -DAPP_URL=http://10.0.2.15:7070/"'''
	}
	stage('Create Candidate Image') {

	    def getVersion = '''docker run --rm \
             -v codeurjc-forge-jenkins-volume:/app \
             -w /app/workspace/\$JOB_NAME/continous_integration/pom_version_updater/ \
             atonich/python3-lxml \
             sh -c "python3 pom-version-updater.py -r ../../pom.xml"'''
	    
	    def currentVersion = sh(returnStdout: true, script: getVersion).trim()
	    def theDate = sh(returnStdout: true, script: 'date +%Y%m%d').trim()
	    tag = currentVersion + '.nightly.' + theDate
	    echo tag

	    //puts the variable into the enviroment
	    env.tag = tag
	    
	    sh ''' mv target/*.jar continous_integration/build/'''
	    sh ''' cd continous_integration/build && ./build-image-with-tag.sh "$tag"'''
	    
	    withDockerRegistry(credentialsId: 'atonich_dockerhub') {
		sh ''' docker push atonich/tic-tac-toe-testing:"$tag"'''
	    }
	}
	stage('Verify Image')
	{
	    env.tag = tag
	    sh '''docker run --rm -d --name nigthly_job \
                -p 7070:8080 \
                atonich/tic-tac-toe-testing:"$tag"'''

	    sh '''docker run --rm \
                -v /var/run/docker.sock:/var/run/docker.sock \
                -v $HOME/.m2:/root/.m2 \
                -v codeurjc-forge-jenkins-volume:/src \
                -w /src/workspace/\$JOB_NAME \
                 maven:3.6.1-jdk-8 \
                 /bin/bash -c \
                   "mvn test -DAPP_URL=http://10.0.2.15:7070/ -DDontStartApp=true -Dtest=TicTacToeWebTest.java"'''
	}
	stage('Publish Nightly Image')
	{
	    env.tag = tag
	    sh '''docker tag atonich/tic-tac-toe-testing:"$tag" atonich/tic-tac-toe-testing:nightly'''
	    withDockerRegistry(credentialsId: 'atonich_dockerhub') {
		sh '''docker push atonich/tic-tac-toe-testing:nightly'''
	    }
	}
    }
    catch (exc){
	echo 'Build Failed'
	currentBuild.result = 'FAILURE'
    }
    finally {
	stage('Result'){
	    archiveArtifacts 'target/*.flv'
	    junit 'target/**/*.xml'
	}
	//clean up
	sh '''git clean -dfx'''
	sh '''docker stop $(docker ps -a --filter "name=nigthly_job" --format "{{ .Names }}")'''
	sh '''docker rmi $(docker images -f "dangling=true" -q)'''
    }
}
