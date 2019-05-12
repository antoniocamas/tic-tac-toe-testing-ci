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
	stage('Build') {
	    // Run the build
	    sh '''docker run --rm \
                    -v $HOME/.m2:/root/.m2 \
                    -v codeurjc-forge-jenkins-volume:/src \
                    -w /src/workspace/\$JOB_NAME \
                    maven:3.6.1-jdk-8 \
                    /bin/bash -c "mvn package -DskipTests"'''
	}
	stage('Test') {
	    // Run the Tests
	    sh '''docker run --rm \
                    -v $HOME/.m2:/root/.m2 \
                    -v codeurjc-forge-jenkins-volume:/src \
                    -w /src/workspace/\$JOB_NAME \
                    maven:3.6.1-jdk-8 \
                    /bin/bash -c "mvn test -Dtest=BoardTest*,TicTacToeGameTest.java"'''
	}
    }
    catch (exc){
	echo 'Build Failed'
	currentBuild.result = 'FAILURE'
    }
    finally {
	stage('Result'){
	    junit 'target/**/*.xml'
	}
	sh 'git clean -dfx'
    }
}
