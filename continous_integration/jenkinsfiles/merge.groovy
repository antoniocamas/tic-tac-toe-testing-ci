node {
    stage('Preparation') {
	// Fetch the changeset to a local branch using the build parameters provided to the
	// build by the Gerrit plugin...
	checkout(
	    [$class: 'GitSCM', branches: [[name: '$GERRIT_BRANCH']],
	     doGenerateSubmoduleConfigurations: false,
	     extensions: [[$class: 'BuildChooserSetting',
			   buildChooser: [$class: 'GerritTriggerBuildChooser']]],
	     submoduleCfg: [],
	     userRemoteConfigs: [[credentialsId: 'hudson_gerrit',
				  refspec: '$GERRIT_REFSPEC',
				  url: 'ssh://10.0.2.15:29418/tic-tac-toe-testing']]
	    ]
	)
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
	stage('SonarQube analysis'){
	    withSonarQubeEnv {
		sh '''docker run --rm \
                  -v $HOME/.m2:/root/.m2 \
                  -v codeurjc-forge-jenkins-volume:/src \
                  -w /src/workspace/\$JOB_NAME \
                  maven:3.6.1-jdk-8 \
                  /bin/bash -c "mvn clean package \$SONAR_MAVEN_GOAL \
                     -DskipTests \
                     -Dsonar.host.url=\$SONAR_HOST_URL \
                     -Dsonar.login=33ed0bb27ac337c11941d22aeaf32f6d80bfe0ae"'''
	    }
	    //sleep needed due to a bug in waitForQualityGate
	    sleep(10)
	    // Job will be killed after a timeout
	    timeout(time: 1, unit: 'HOURS') {
		def qg = waitForQualityGate() 
		if (qg.status != 'OK') {
		    error "Pipeline aborted due to quality gate failure: ${qg.status}"
		}
	    }
	    withSonarQubeEnv {
		sh '''./continous_integration/sonarqube/event_create_api_rest.sh \
                      -v --sq-url \$SONAR_HOST_URL \
                      --sq-report target/sonar/report-task.txt \
                      --git-ref \$GERRIT_PATCHSET_REVISION'''
	    }
	    archiveArtifacts 'target/sonar/report-task.txt'
	}
	stage('Docker Image') {
	    withDockerRegistry(credentialsId: 'atonich_dockerhub') {
		sh ''' mv target/*.jar continous_integration/build/'''
		sh ''' cd continous_integration/build && ./build-image-with-tag.sh dev'''
		sh ''' docker push atonich/tic-tac-toe-testing:dev'''
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
	sh 'git clean -dfx'
	sh '''docker rmi $(docker images -f "dangling=true" -q)'''
    }
}
