# ZHAW Timetable

Lightweight web app that serves timetables for students of ZHAW.

Access the timetable using your id:

    https://anliksim.github.io/zhawtt/#/<zhawid>


### Development 

1. Run `lein do clean, figwheel` for development
2. Open `http://localhost:3449/`

Any changes to `src` will be re-compiled and picked up by figwheel.

To optimize for production run `lein do clean, with-profile prod compile`.

