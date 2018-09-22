# ZHAW Timetable Page

Lightweight web app that serves timetables for students of ZHAW.

### Usage 

1. Run "`lein do clean, figwheel`" for development
2. Open `http://localhost:3449/`

Any changes to `src` will be re-compiled and picked up by figwheel.

To optimize for production run "`lein do clean, with-profile prod compile`".
