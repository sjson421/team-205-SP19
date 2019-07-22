## Preparation
### Pros and cons compared with Heroku
The example professor gave us is AWS, Jenkins is ready to deploy on AWS, it will be the easiest way to deploy with AWS. The difficulties are about how to efficiently configurate in AWS (perhaps using cloudformation) to use least resources (like only one EC2 instance) and with enough AWS native features (cloudwatch for logs and metrics, routing, IAM, storage and database).

### Registration
We need a account for AWS (Basic Plan), and don't forget to use [AWS education](https://aws.amazon.com/education/awseducate/) which should cover most of our cost with free tier.

### Necessary component for our project
* EC2 or lambda: In our use case, we should use EC2 rather than lambda. lamda is a serverless AWS service, EC2 is Elastic Compute Cloud, giving us a virtual computer to run our application. It is not worth it to break down current project and move to lambda now. We will need to config EC2 with jenkins to achieve automated deployment for master commits of github.
* S3: Amazon's simple cloud storage service, should be our place to storage large files like videos when we need it.
* DynamoDB and RDS: For use cases where we want more complicated and flexible data structure, DynamoDB, AWS's NoSQL service could help us a lot, while RDS should be a good start point.
* IAM: We should setup our User, Group and Roles for AWS so that we can have appropriate identity to do different work. Role is something that any team member could use to some certain job.
* CloudWatch: We need to demonstrate logs on CW, we will need to add metrics and set up dashboard to surveil some significant performance, we may need to add alarm so that we could get notification instantly when something is wrong.
* CodeDeploy: We want to have a timestamp for each code deploy, we should figure out either Jenkins or CodeDeploy gives us such feature.

We could using US east-1 as our default locale
### Create Key Pair
Create key pair at [console](https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#KeyPairs:sort=keyName)

### CodeDeploy
We will not deploy code manully, but if let jenkins to do the exact same work, should we also get some log here?
* Go to [console](https://console.aws.amazon.com/codesuite/codedeploy/applications?region=us-east-1)

### Create an EC2 instance
* choose t2.micro since it is the free tier. We will need the priavte key to have access to it.
* After instace state change from pending to running, we will be able to SSH this instance and use it.
![Public DNS and Public IP](https://i.imgur.com/zfdg9Kq.png)

### SSH
You will need your pem and public IP of EC2 instance.
> ssh -i {full path of your .pem file} ec2-user@{instance IP address}

ec2-user is our default user name. We may want to add [more users](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/managing-users.html)

Result:

![](https://i.imgur.com/5QNt63x.png)

### Deploy
We need to make sure those jenkins steps can run
>     sh 'cd ${WORKSPACE}'
    sh "chmod 400 ${json.server[0].PEM}"
    sh "scp -oStrictHostKeyChecking=no -i ${json.server[0].PEM} Development/ChatServer/target/${json.server[0].JARNAME} ${json.server[0].user}@${json.server[0].DNS}:${json.server[0].directory}"
    sh "ssh -oStrictHostKeyChecking=no -i ${json.server[0].PEM} ${json.server[0].user}@${json.server[0].DNS}  pkill java &"
    sh "ssh -oStrictHostKeyChecking=no -i ${json.server[0].PEM} ${json.server[0].user}@${json.server[0].DNS}  nohup java -jar ${json.server[0].directory}/${json.server[0].JARNAME} >nohup.out 2>&1 &"


* Result a normal CodeDeploy
![](https://i.imgur.com/DUB7zBX.png)
![](https://i.imgur.com/MbNfOhQ.png)

### Conclusion
The point is make sure IAM role is available for deploy, and pem file with right WORKSPACE should be able to use the jar file Jenkins creates before.

You may want to terminate EC2 instance as long as it is NOT the instance where our application runs.