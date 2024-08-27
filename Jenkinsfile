pipeline {
    agent any 
    tools {
        gradle 'gradle8'
    }
     environment {
        CI = 'false'
    }    
    stages {
   //     stage('Removing Old Data') {
   //         steps {
   //             script {
    //                sh 'rm -rf /opt/ems/ui/*'
     //               sh 'rm -rf /opt/ems/identity/*'
      //              sh 'rm -rf /opt/ems/employee/*'
      //          }
       //     }
      //  }
        stage('Install Dependencies') {
            steps {
                script {
                    dir('ui') {
                        sh 'npm install'
                    }
                }
            }
        }
        stage('Build UI') {
            steps {
                script {
                    dir('ui') {
                        sh 'npm run build'
                    }
                }
            }
        }
        stage('Deploy UI') {
            steps {
                script {
                    sh 'rm -rf /var/www/ems/* -R'
                    sh 'chown -R jenkins:jenkins /var/www/ems/'
                    sh 'cp -R /opt/ems/ui/build/* /var/www/ems/'
                }
            }
        }
        stage('Build Identity') {
            steps {
                script {
                    dir('identity') {
                        sh 'gradle clean build'
                        sh 'cp -R /var/lib/jenkins/workspace/ems/identity/build/* /opt/ems/identity/'
                    }
                }
            }
        }
        stage('Package Identity') {
            steps {
                script {
                    dir('identity') {
                        sh 'gradle assemble'
                    }
                }
            }
        }
        stage('Build Employee') {
            steps {
                script {
                    dir('employee') {
                        sh 'gradle clean build'
                        sh 'cd /var/lib/jenkins/workspace/ems/employee/build'
                        sh 'cp * /opt/ems/employee/ -R'
                    }
                }
            }
        }
        stage('Package Employee') {
            steps {
                script {
                    dir('employee') {
                        sh 'gradle assemble'
                    }
                }
            }
        }
    }
}
