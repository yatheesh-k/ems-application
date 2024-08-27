pipeline {
    agent any 
    tools {
        gradle 'gradle8'
    }
     environment {
        CI = 'false'
    }    
    stages {
        stage('Removing Old Data') {
            steps {
                script {
                   sh 'sudo rm -rf /opt/ems/identity/* -R'
                   sh 'sudo rm -rf /opt/ems/employee/* -R'
               }
          }
       }
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
                    sh 'sudo chown -R jenkins:jenkins /var/www/ems/'
                    sh 'sudo rm -rf /var/www/ems/* -R'
                    sh 'sudo chown -R jenkins:jenkins /var/www/ems/'
                    sh 'sudo cp -R /var/lib/jenkins/workspace/ems/ui/build/* /var/www/ems/'
                }
            }
        }
        stage('Build Identity') {
            steps {
                script {
                    dir('identity') {
                        sh 'gradle clean build'
                        sh 'sudo cp -R /var/lib/jenkins/workspace/ems/identity/build/* /opt/ems/identity/'
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
                        sh 'sudo cp -R /var/lib/jenkins/workspace/ems/employee/build/* /opt/ems/employee/'
                    }
                }
            }
        }
 //       stage('Package Employee') {
   //         steps {
     //           script {
       //             dir('employee') {
         //               sh 'gradle assemble'
           //         }
//                }
  //          }
    //    }
    }
}
