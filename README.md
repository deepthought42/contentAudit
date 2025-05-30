# Content Audit

This project implements a content audit system using Spring Boot, with integration for Google Cloud Platform (GCP).

## AuditController

The `AuditController` is a key component of this application, responsible for:

1. Initiating content audits
2. Retrieving audit results
3. Managing audit configurations

It provides RESTful endpoints for interacting with the audit system, allowing users to start audits, check their status, and retrieve results.

## Configuration

This project uses GCP Secret Manager for secure configuration management. The main configuration files are:

- `application.properties`

### Setting up GCP Secrets

1. Create a GCP project if you haven't already.
2. Enable the Secret Manager API for your project.
3. Create secrets in Secret Manager for each property in `application.properties`.
4. Update the `spring.cloud.gcp.secretmanager.secret-name-prefix` in your local `application.properties` to match your GCP project ID.

### GCP Project Credentials

To authenticate with GCP services:

1. Go to the GCP Console > IAM & Admin > Service Accounts.
2. Create a new service account or select an existing one.
3. Generate a new JSON key for the service account.
4. Save the JSON file securely on your local machine.
5. Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path of this JSON file:

   ```
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials.json
   ```

### Gmail Credentials

To use Gmail API for email-related features:

1. Go to the Google Cloud Console.
2. Enable the Gmail API for your project.
3. Create OAuth 2.0 credentials (OAuth client ID).
4. Download the JSON file for the OAuth 2.0 client ID.
5. Save this JSON file securely and update the `gmail.credentials.file` property in your `application.properties` with the file path.

## Running the Application

1. Ensure all GCP secrets are properly configured.
2. Set up the GCP project credentials and Gmail credentials as described above.
3. Build the project using Maven:

   ```
   mvn clean install
   ```

4. Run the application:

   ```
   java -jar target/contentAudit-0.0.1-SNAPSHOT.jar
   ```

## API Documentation

Once the application is running, you can access the Swagger UI for API documentation at:

```
http://localhost:8080/swagger-ui.html
```

This will provide detailed information about the available endpoints and how to use them.

### PageAuditMessage Object Schema

The `PageAuditMessage` object represents a message that is passed to the AuditController endpoint for processing page audits. It contains essential information about the audit request and is used to initiate and track individual page audits. The object has the following fields:

- `pageAuditId` (String): The unique identifier for the page audit.
- `pageId` (String): The identifier of the page being audited.
- `messageId` (String): The unique identifier for the message.
- `publishTime` (DateTime): The timestamp when the message was published.
- `accountId` (String): The identifier of the account associated with the message.

This object is used in various API endpoints related to page audits and messaging, particularly when submitting new audit requests to the AuditController.

## Security

Authentication is not currently implemented in this application. Features such as OAuth, SAML, or other authentication mechanisms will need to be added to secure the system. Please be sure to implement appropriate authentication and authorization before deploying to production.

## Support

For any issues or questions, please open an issue in the project repository.

