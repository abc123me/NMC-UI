# NMC Programming standards
<center>Jeremiah Lowe, Programmer</center>
<center>Version 0.1</center>
## MySQL database
### Database configuration
 - All cables **MUST** be colored **RED** and labeled on each end, if not the cable **WILL** be disconnected form the server
 - Server must have ethernet failover configured
 - Database port is default (3306 TCP)
 - Database IP will be the interface connected to the server's closed subnet
 - The database should be on the UTC timezone
### Database naming conventions
 - Inside tables CamelCasing should be used for all column names
    - Just like Java class names, for example: `DateTime, HelloWorld, CamelCasing, Test123`
    - If a measurement has multiple accepted units then it should be in the format `TempRoomF, TempRoomC, PowerW, PowerHP`
 - The first column is the date and it must be in one of the following formats:
    - Column 1 named `DateTime` of type `DATETIME` containing the time in MySQL `DATETIME` format
    - Column 1 named `EpochMS` of type `BIGINT` containing the UNIX time in milliseconds
    - ***ANY OTHER CONFIGURATIONS WILL NOT BE ACCEPTED***
 - All tables inside should be named after the accepted abbreviations of  the subsystems they contain data for
    - Database names should be all uppercase and end with `_TBL`
    - If the subsystem's name is only 1 word then that word is acceptable
   - For instance the waveguide dehydrator should be named `WGD_TBL`, Feed becomes `FEED_TBL`
 - Two seperate databases
    - Staging used for testing purposes `staging.db`
    - Production database named `main.db`
 - Usernames must follow standard linux username format (`^[a-z0-9_]+$`, alphanumeric, lowercase, underscores)
### Database security
 - Subsystems should each have thier own login credentials and only access to thier table, eg: `GRANT feed@feed_IP * ON FEED_TBL;`
    - Prevents a subsystem from accidentally deleting or overwriting another subsystems data
    - If the credentials are leaked the entire system don't have to re-done
 - Database's subnet to be **COMPLETELY** closed from outside world
 - The UI has a reserved account which shall only be granted read privlidges
    - The UI may have its own table for logging purposes but should only be able to `INSERT` and `SELECT`