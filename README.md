# ANTIGRAV BOOTS

Antigrav Boots is a utility that allows you to quickly merge comma separated value (CSV) files from the
Windows Explorer context menu.

![image](https://github.com/scgrn/ANTIGRAV-BOOTS/blob/main/images/screenshot.png)
If you're just looking for a ready-to-use binary, you can find one [here](https://github.com/scgrn/ANTIGRAV-BOOTS/releases/download/v1.0.0/Antigrav.Boots.exe).

## Usage:
Run the executable once without arguments to install. It will update the registry and
create a shortcut link in your user folder.

After installation, shift or ctrl-click several CSV files from windows explorer. Right click,
select send to->combine csv files, specify an output filename, and you're off to the races.

## Building:

To build, use a Java 8+ JDK and [Apache Ant](https://ant.apache.org/) version 1.8 or above.

An XML descriptor for [Launch4j](https://launch4j.sourceforge.net/) is included to build an executable. It is
currently required that the compiled jar be wrapped into an executable for
the program to operate correctly.

## Dependencies:
[opencsv](http://opencsv.sourceforge.net/)

[mslinks](https://github.com/DmitriiShamrikov/mslinks])


Opencsv is dependent on several libraries from [Apache Commons](https://commons.apache.org/):

&nbsp;&nbsp;&nbsp;&nbsp;[commons-beanutils](https://commons.apache.org/proper/commons-beanutils/)
	
&nbsp;&nbsp;&nbsp;&nbsp;[commons-lang](https://commons.apache.org/proper/commons-lang/)
	
&nbsp;&nbsp;&nbsp;&nbsp;[commons-text](https://commons.apache.org/proper/commons-text/)
	
Opencsv  also requires [commons-collection](https://commons.apache.org/proper/commons-collections/) but it's omitted here because I'm not using any
of its functionality.
	
## License
Distributed under the MIT License. See [`LICENSE.txt`](https://github.com/scgrn/ANTIGRAV-BOOTS/blob/main/LICENSE) for more information.

## Contact
Andrew Krause - ajkrause@gmail.com

Project Link: [https://github.com/scgrn/ANTIGRAV-BOOTS](https://github.com/scgrn/ANTIGRAV-BOOTS)

