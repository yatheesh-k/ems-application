pipeline {
  agent any 
  tools {
        gradle 'gradle8'
  }
  stages ('removing old data') {
    steps {
      script {
        sh 'rm -rf /opt/ems/ui/*'
        sh 'rm -rf /opt/ems/identity/*'
        sh 'rm -rf /opt/ems/employee/*'
      }
    }
     stage ('install dependencies') {
    steps {
      script {
        dir('ui') {
          sh 'npm install'
        }
      }
    }
  }
  stage ('Build ui') {
    steps {
      scripts {
        dir('ui') {
          sh 'npm run build'
        }
      }
    }
  }
  stage('Deploy ui') {
          steps {
                script {                    
                    sh "sudo rm -rf /var/www/ems/*"
                    sh "sudo cd /opt/ems/ui/build/"
                    sh "sudo cp * /var/www/ems/ -R"
                }
          }
  }
  
   stage ('Build identity') {
   steps {
     script {
       dir('identity') {
            sh 'gradle clean build'
            sh 'cd /var/lib/jenkins/workspace/ems/identity/build'
            sh 'cp * /opt/ems/identity/ -R'
       }
     }
   }
  }
  stage ('package identity') {
  steps {
    script {
      dir('identity') {
        sh 'gradle assemble'
      }
    }
  }
  }
     stage ('Build employee') {
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
  stage ('package employee') {
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
