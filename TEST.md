# Manual testing

The following points should be tested after changes.

* Upload large files without OOME
* Upload many files at once
* Check polling of ng_sftp is not blocked by upload of large files
* Start and success entries in ng_events table are inserted
* Configure wrong sftp password and check for failure entry in ng_events table
* Test resend of files