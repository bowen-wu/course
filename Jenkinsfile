def responseJson = new URL("http://101.35.43.9:5000/v2/test-jenkinsfile/tags/list").getText(requestProperties: ['Content-Type': "application/json"]);


// responseJson: {name:xxx,tags:[tag1,tag2,...]}
Map response = new groovy.json.JsonSlurperClassic().parseText(responseJson) as Map;

def versionsStr = response.tags.join('\n');

pipeline {
    agent any
    stages {
        stage('Deploy') {
             environment {
                SSH_CREDS = credentials('f426a4f3-d52e-4737-ad44-65ed0586e7e7')
            }
            input {
                message "Choose a version"
                ok "Deploy"
                parameters {
                    choice(choices: versionsStr, description: 'version', name: 'version')
                }
            }
            steps {
                sh 'echo "SSH private key is located at $SSH_CREDS"'
                sh 'echo "SSH user is $SSH_CREDS_USR"'
                sh 'echo "SSH passphrase is $SSH_CREDS_PSW"'
                echo "🎉 You choose version: ${version} 🎉"
                sh "ssh root@101.35.43.9 'docker pull 101.35.43.9:5000/test-jenkinsfile:${version}'"
                echo "🎉 Pull 101.35.43.9:5000/test-jenkinsfile:${version} Success~ 🎉"
                sh "ssh root@101.35.43.9 'source /root/project/course/start-docker-container.sh ${version}'"
                echo "🎉 Restart Success~ 🎉"
            }
        }
    }
}
