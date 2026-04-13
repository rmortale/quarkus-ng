# Manual testing

The following points should be tested after changes.

* Upload large files without OOME
* Upload many files at once
* Start and success entries in ng_events table are inserted
* Configure wrong sftp password and check for failure entry in ng_events table
* Resend file wich no longer exists in minio. check for correct error handling
* Test resend of files
* Test service with no routing in db
* 