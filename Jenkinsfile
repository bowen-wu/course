String buildNumber = env.BUILD_NUMBER;
String timestamp = new Date().format('yyyy-MM-dd_HH-mm-ss', TimeZone.getTimeZone('Asia/Shanghai'));
String projectName = env.JOB_NAME.split(/\//)[0];
String version = "${buildNumber}_${timestamp}_${projectName}";

pipeline {
    agent any
    triggers {
       pollSCM('* * * * *')
    }
    stages {
        stage('Test') {
            agent {
                docker {
                    image 'maven:3.8.1-adoptopenjdk-11'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn clean verify -DskipTests'
                echo '🎉 Verify Success 🎉'
                stash includes: '**/target/*.jar', name: 'app'
            }
        }
        stage('Docker Build') {
            steps {
                echo 'Starting to build docker image'

                unstash 'app'
                script {
                     def customImage = docker.build("101.35.43.9:5000/course:${version}")
                     echo '🎉 Docker Build Success 🎉'
                     customImage.push();
                     echo '🎉 Push Success 🎉'
                }
            }
        }
    }
}
