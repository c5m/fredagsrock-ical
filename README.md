Scala script to fetch concert dates from [Tivoli's](fredagsrock.dk) website  
and create an `ical` file which can be imported into your calenar.

To run you need [Scala CLI](https://scala-cli.virtuslab.org/):
```sh
scala-cli main.sc
```

This will output a `fredagsrock-<year>.ical` file.