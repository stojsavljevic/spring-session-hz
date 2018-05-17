# Session Sharing

This is a demo application (`spring-session-hz`) with session sharing implemented using Spring Session and Hazelcast as a data store.

Test module tries to reproduce race conditions and benchmarks the implementation.

In order to use this application you have to:

* start two instances of `spring-session-hz-demo` with different properties (pass `--spring.config.location=PATH_TO_PROP_FILE` on startup).
Example properties file is located in `src/main/resources`.
Properties that have to be different:
  * `server.port` - set 8080 and 9090
  * `hz.port` - set 5721 and 5722

* run `SessionSharingTests` from `spring-session-hz-test`

If test completed successfully it means no race condition was detected.

In the log file you'll get average duration of one test cycle, e.g:

```
c.alex.session.test.SessionSharingTests  : AVERAGE EXECUTION TIME: 5156ms
```
