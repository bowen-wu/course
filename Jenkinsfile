def dockerRegistryIp = "101.35.43.9";
def projectName = "course";
def jenkinsSSHCredentialId = "";
def responseJson = new URL("http://${dockerRegistryIp}:5000/v2/${projectName}/tags/list").getText(requestProperties: ['Content-Type': "application/json"]);

// responseJson: {name:xxx,tags:[tag1,tag2,...]}
Map response = new groovy.json.JsonSlurperClassic().parseText(responseJson) as Map;

def versionsStr = response.tags.join('\n');

pipeline {
    agent any
    stages {
        stage('Deploy') {
            input {
                message "Choose a version"
                ok "Deploy"
                parameters {
                    choice(choices: versionsStr, description: 'version', name: 'version')
                }
            }
            steps {
                echo "🎉 You choose version: ${version} 🎉"
                sshagent (credentials: ["$jenkinsSSHCredentialId"]) {
                    sh "ssh -o StrictHostKeyChecking=no root@${dockerRegistryIp} 'docker pull ${dockerRegistryIp}:5000/${projectName}:${version}'"
                    echo "🎉 Pull ${dockerRegistryIp}:5000/${projectName}:${version} Success~ 🎉"
                    sh "ssh -o StrictHostKeyChecking=no root@${dockerRegistryIp} 'source /root/project/course/start-docker-container.sh ${version}'"
                    echo "🎉 Restart Success~ 🎉"
                }
            }
        }
    }
}
