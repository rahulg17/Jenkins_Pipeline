node {
  try {
    //def mvnHome = "/usr/share/maven"
    // send to HipChat
    hipchatSend (color: 'YELLOW', notify: true,
    message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")

   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git 'https://github.com/rahulg17/CreditCardApplication.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.
      //mvnHome = tool 'M3'
   }
   
   // TDD job edit here
   
   
   stage('Update DB') {
    // sh  "mysql -h 127.0.0.1 -u root -pMicrosoft@12 < /opt/insert.sql"
   }
   stage('Sonarqube Analysis') {
       sh 'mvn sonar:sonar'
   }
   stage('Build') {
      // Run the maven build
      if (isUnix()) {
         sh "mvn -Dmaven.test.failure.ignore clean package"
      } else {
         bat(/mvn -Dmaven.test.failure.ignore clean package/)
      }

      hipchatSend (color: 'GREEN', notify: true,
      message: "BUILD COMPLETED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
   }
   stage('Quality scan & Test results') {
      junit '**/target/surefire-reports/TEST-*.xml'
      archive 'target/*.jar'
   }
   stage('Ansible Deploy [DEV]') {
      sh "sudo su devopsuser /opt/workspace/devops-tooling/jenkins-ansible-deploy.sh 'deploy-dev'"
      
      hipchatSend (color: 'GREEN', notify: true,
      message: "DEV DEPLOYED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
      
   }

    stage('Selenium Functional Test - After Deploy') { // for display purposes
      // Get some code from a GitHub repository
      //sh 'export DISPLAY=:99'
      git 'https://github.com/rahulg17/selenium-test.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.
      //mvnHome = tool 'M3'\
      sh 'mvn test -Dtest=SeleniumHeadlessTest'
   }



   stage('Ansible Deploy [SIT]') {
      sh "sudo su devopsuser /opt/workspace/devops-tooling/jenkins-ansible-deploy.sh 'deploy-test'"
      
      hipchatSend (color: 'GREEN', notify: true,
      message: "SIT DEPLOYED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")

   }
  //  sms notification
   //sh 'curl -G "http://smshorizon.co.in/api/sendsms.php" -v -L --data-urlencode "user=avinash12" --data-urlencode "apikey=1U96X0kBJpd1y3XCeloH" --data-urlencode "mobile=+919953064505" --data-urlencode "message=Credit Card Application Deployed Successfully on Dev Env." --data-urlencode "senderid=MYTEXT" --data-urlencode "type=txt"'


  } catch (e) {
      hipchatSend (color: 'RED', notify: true,
      message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})" )
      exit 1
    // sms notification
      //sh 'curl -G "http://smshorizon.co.in/api/sendsms.php" -v -L --data-urlencode "user=ajitrajput" --data-urlencode "apikey=OzbGQUlz5QRnyRvf5Da7" --data-urlencode "mobile=+919158001659" --data-urlencode "message=Credit Card Application Failed." --data-urlencode "senderid=188860" --data-urlencode "type=txt"'
  }
}

