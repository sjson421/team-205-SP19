# Goal
During continous deployment, timestamping is necessary for checking when each commit was made. This information is required because otherwise, keeping track of team progress becomes exponentially difficult.

## Technologies

* Netlify: This technology was first recommended to us by the TA. However, Netlify is less a technology for recording timestamps than a full web hosting service. We do not have the infrastructure required yet for such a technology, so this is not what we need.

* Clockwork for Jira: I researched this out of a misunderstanding of the ticket; I thought that the timestamping was for Jira and not for the cloud platform. As for performance, Clockwork is a very useful plugin for Jira that allows for both timestamping as well as automatic tracking of how long a ticket took to move from creation or from its status as a To Do ticket to a Completed one. Although this would be useful, it is not what we need.

* CloudWatch for AWS: Since we would be using AWS, CloudWatch would be the go-to solution for tracking and monitoring the instances of AWS. Using CloudWatch would allow for timestamping as well as a plethora of other functionalities, including monitoring data, utilization, and changes. With event visibility of our entire project in one place, optimization and and resource planning tasks would become much simpler. However, we will not use CloudWatch for reasons stated in the Conclusion.

# Conclusion
On our meeting with the TA on March 1st, 2019, we were informed that by deploying to AWS, timestamping would be automatically recorded. Therefore, this information is unnecessary for our project as of now.