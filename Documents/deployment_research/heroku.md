## The complications with deploying to Heroku

## Preparation
### Pros and cons compared with AWS
The example professor gave us is AWS, Jenkins is ready to deploy on AWS, it will be the easiest way to deploy with AWS. The difficulties are about how to efficiently configurate in AWS (perhaps using cloudformation) to use least resources (like only one EC2 instance) and with enough AWS native features (cloudwatch for logs and metrics, routing, IAM, storage and database).

Pros:
    - Heroku offers easy local deployment setups, as well as integrations with Github, and the Heroku Container registry as possible deployment options

Cons:
    - Deploying to Heroku locally is not the most optimal solution -> It requires someone to manually handle deployment
    - Deploying an image of the code base to Heroku Container Registry is an option as well, but that also needs to be done locally or on a deployment machine that has Heroku CLI
    - Deploying via the Github plugin is by far the easiest and best solution for us. Upon push to master, the plugin will detect changes and push to the Heroku instance
        - Doing it this way will not fix the fact that Jenkins errors out on deployment. Basically we would need to remove the deploy step in the Jenkinsfile, because the plugin would handle deployment. Changing the Jenkinsfile is _strongly_ advised against by the professor and TAs.
    

### What if we want to use Jenkins to deploy to Heroku?
- This involves a lot of configuration with the machine that jenkins is running on
    - Per http://www.be9.io/2015/12/01/heroku-deployment/, we need to change Jenkins and add Heroku
    - We would need to install Heroku CLI on the same machine
    - Then change the Jenkinsfile to use Heroku CLI to push to Heroku

These changes are likely out of our control, since we don't have the permissions to change the Jenkins server


### Conclusion
There are a few ways to make Heroku an option for us, but all options would involve some change in the Jenkinsfile, which isn't an option. Only the AWS option will allow deployments to work out of the box, without us having to change the Jenkinsfile.