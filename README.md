#`TimeField` Swing Component

![Time/Duration Field](https://cloud.githubusercontent.com/assets/10276932/13558648/cfe5cca0-e45c-11e5-961f-09c96514d2b9.png)

*This is a proof of concept, designed the demonstrate a possible solution to
initial requirements, which was used to gain feedback and drive actual
development*

Some time ago I had a client that was unimpressed with `JFormattedTextField` and
`JSpinner` when it came to entering in time values.  Coming from a pre-existing
text based system, they wanted to be able to at least replicate the functionality
that the pre-existing system had, if not exceed it.

The field needed to provide:

* Formatted in `hh:mm a` or `HH:mm`
* Real time validation
  * This would mean limiting what the user could enter based on a series of rules
including, if set to 12 hour time, if the use typed `1` in the hour field, they
could only enter `0`, `1` or `2`, with simular restrictions on the minutes 
and seconds fields
* Simple navigation, moving from the hours portion to the seconds portion when
the user exceeds the number of digits for the field (ie typing `03` should move 
the user to the seconds portion with them needing to do anything), they should 
also be able to arrow key back and forwards like a normal field, moving between
the different time portions.

The implementation also provided

* The ability to [Tab] between field portions
* Use the [:] to move from the hour to seconds portions, which allowed for a
more natural typing experience (`1:52` for example)

A further enhancement was made later to introduce a concept of a `DurationField`
which allowed the entry of any number of hours, but which still restricted the
minutes (and potentially seconds)

The code is intended to demonstrate the possibilities for a time/duration
field based on some basic requirements.

Future enhancements would include:

* The ability to configure seconds and milliseconds
* The ability to change the time separators based on localization

