String buildNumber = env.BUILD_NUMBER;
String timestamp = new Date().format('yyyy-MM-dd_HH-mm-ss', TimeZone.getTimeZone('Asia/Shanghai'));
String projectName = env.JOB_NAME.split(/\//)[0];
String version = "${buildNumber}_${timestamp}_${projectName}";

pipeline {
    // TODO: 此处添加 maven 缓存会导致 jenkins 失败
//     agent {
//         docker {
//             image 'maven:3.8.1-adoptopenjdk-11'
//             args '-v $HOME/.m2:/root/.m2'
//             reuseNode true
//         }
//     }
    agent any
    triggers {
       pollSCM('* * * * *')
    }
    stages {
        stage('Test') {
            agent {
                docker { image 'circleci/openjdk:8u212-jdk-stretch' }
            }
            steps {
                sh 'mvn clean verify'
                echo '🎉 Verify Success 🎉'
            }
        }
        stage('Docker Build') {
            steps {
                echo 'Starting to build docker image'

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
