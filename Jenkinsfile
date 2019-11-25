pipeline {
    agent {
        dockerfile {
            args '-v ${HOME}/.m2:/home/builder/.m2 -v ${HOME}/bin:${HOME}/bin'
            additionalBuildArgs '--build-arg BUILDER_UID=$(id -u)'
        }
    }
    stages {
        stage('package') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
    post {
        success {
            dir('target/') {
                archiveArtifacts artifacts: '*.jar', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
}
