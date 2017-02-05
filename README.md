[![Build Status](https://travis-ci.org/mozq/picto.svg?branch=master)](https://travis-ci.org/mozq/picto)
[ ![Download](https://api.bintray.com/packages/mozq/generic/picto/images/download.svg) ](https://bintray.com/mozq/generic/picto/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Picto

Picto is a desktop application for organizing photo files.

It can be used for the following.

- Copy or move photos to another folder under a different name.
    - You can specify sub folders and file names freely at shooting date etc.
- Change file date (Creation date, Modified date, Access date, Exif date).
    - Specified date and difference date can be set. It is effective when you forget to change the time zone of the camera.
- Remove GPS or all Exif data.


## Installation

### for Mac
1. Download DMG file 'Picto-X.X.X-mac.dmg' from [here](https://bintray.com/mozq/generic/picto/_latestVersion). (X.X.X is a version)
2. Mount DMG and copy Picto.app to Applications folder.
3. Execute Picto.app to start.
    - If the warning dialog of "can’t be opened because it is from an unidentified developer" is shown, try the following procedure
        1. Right-click (or control-click) the app and choose “Open”.
        2. Click the “Open” button at the next dialog warning to launch the app anyway

### for Windows
1. Install [Java Runtime Environment](https://java.com/ja/download/), if it is not installed.
2. Download Zip file 'Picto-X.X.X-win.zip' from [here](https://bintray.com/mozq/generic/picto/_latestVersion). (X.X.X is a version)
3. Unzip and copy Picto.exe to any folder.
4. Execute Picto.exe to start.

### for other OS
1. Install [Java Runtime Environment](https://java.com/ja/download/), if it is not installed.
2. Download JAR file 'Picto-X.X.X-all.jar' from [here](https://bintray.com/mozq/generic/picto/_latestVersion). (X.X.X is a version)
3. Copy JAR file to any folder.
4. Execute JAR file like `java -jar Picto-X.X.X-all.jar`.


## Uninstallation
You can uninstall application by deleting the copied application file.


## Settings

### Source

#### Photo's folder
Select folder contains picture and movie files.

#### File Pattern
Filter files by filename.
You can use [Glob](https://en.wikipedia.org/wiki/Glob_%28programming%29) or [Regular expression (Regex)](https://en.wikipedia.org/wiki/Regular_expression) pattern.

- Glob: "*.jpg", "IMG_????.*"
- Regex: ".*\\.jpg", "IMG_[0-9]{4}\\..*"

#### Contains hidden files and folders.
When check this option then contains hidden files and folders.

#### Contains sub files and folders.
When check this option then contains sub files and folders.

#### File Size
Filter files by file size.

#### Creation Time
Filter files by creation time.

#### Modified Time
Filter files by modified time.


### Destination

#### Operation
- Copy -- Copy files in Photo's folder to Destination Folder.
- Move -- Move files in Photo's folder to Destination Folder.
- Overwrite -- Overwrite files in Photo's folder.

#### Destination Folder
Copy or move destination folder.

#### Sub Path Pattern
File's sub path in Destination Folder.
You can use following variables with '${}'.

    e.g.)
    "${ParentSubPath}/${PhotoTakenDate%uuuu/MM}/${FileName}"
    -> "subpath/to/2012/01/IMG_0001.JPG"

|Variable                 |Type    |Meaning                          |Examples                |
|-------------------------|--------|---------------------------------|------------------------|
|Now                      |Date    |Current date                     |2012-01-23 12:34:56.780 |
|ParentSubPath            |String  |Parent folder path of file       |subpath/to              |
|FileName                 |String  |File name                        |IMG_0001.JPG            |
|BaseName                 |String  |File base name                   |IMG_0001                |
|Extension                |String  |File extension                   |.JPG                    |
|Size                     |String  |File size                        |10485760                |
|CreationDate             |Date    |File creation date               |2012-01-23 12:34:56.780 |
|ModifiedDate             |Date    |File modified date               |2012-01-23 12:34:56.780 |
|AccessDate               |Date    |File access date                 |2012-01-23 12:34:56.780 |
|PhotoTakenDate           |Date    |Exif date or file modified date  |2012-01-23 12:34:56.780 |
|Width                    |Integer |Exif image width                 |6000                    |
|Height                   |Integer |Exif image height                |4000                    |
|FNumber                  |Decimal |Exif FNumber                     |8.0                     |
|Aperture                 |Decimal |Exif aperture value              |6.0                     |
|MaxAperture              |Decimal |Exif max aperture value          |3.6                     |
|ISO                      |Integer |Exif ISO                         |100                     |
|FocalLength              |Decimal |Exif focal length                |26.0                    |
|FocalLength35mm          |Decimal |Exif focal length in 35mm format |39.0                    |
|ShutterSpeed             |Decimal |Exif shutter speed value         |7.0                     |
|ExposureTime             |Decimal |Exif exposure time (Seconds)     |0.008                   |
|ExposureMode             |Integer |Exif exposure mode               |0: Auto exposure<br>1: Manual exposure<br>2: Auto bracket |
|ExposureProgram          |Integer |Exif exposure program            |0: Not defined<br>1: Manual<br>2: Normal program<br>3: Aperture priority<br>4: Shutter priority<br>5: Creative program<br>6: Action program<br>7: Portrait mode<br>8: Landscape mode |
|Brightness               |Integer |Exif brightness value            |2.21                    |
|WhiteBalance             |Integer |Exif white balance               |0: Auto<br>1: Manual    |
|LightSource              |Integer |Exif light source                |0: Unknown<br>1: Daylight<br>2: Fluorescent<br>3: Tungsten (incandescent light)<br>4: Flash<br>9: Fine weather<br>10: Cloudy weather<br>11: Shade<br>12: Daylight fluorescent (D 5700 - 7100K)<br>13: Day white fluorescent (N 4600 - 5400K)<br>14: Cool white fluorescent (W 3900 - 4500K)<br>15: White fluorescent (WW 3200 - 3700K)<br>17: Standard light A<br>18: Standard light B<br>19: Standard light C<br>20: D55<br>21: D65<br>22: D75<br>23: D50<br>24: ISO studio tungsten<br>255: Other light source |
|Orientation              |Integer |Tiff orientation                 |[0th Row, 0th Column]<br>1: Top, Left side<br>2: Top, Right side<br>3: Bottom, Right side<br>4: Bottom, Left side<br>5: Left side, Top<br>6: Right side, Top<br>7: Right side, Bottom<br>8: Left side, Bottom |
|Lens                     |String  |Exif lens                        |                        |
|LensMake                 |String  |Exif lens make                   |                        |
|LensModel                |String  |Exif lens model                  |                        |
|LensSerialNumber         |String  |Exif lens serial number          |                        |
|Make                     |String  |Tiff make                        |NIKON CORPORATION       |
|Model                    |String  |Tiff model                       |NIKON D1                |
|Software                 |String  |Exif software                    |Capture NX-D 1.2.0 M    |
|ProcessingSoftware       |String  |Exif processing software         |                        |
|OwnerName                |String  |Exif owner name                  |                        |
|CameraOwnerName          |String  |Exif camera owner name           |                        |
|GPSLat                   |Decimal |Exif GPS latitude degrees north  |35.658581               |
|GPSLatDeg                |Decimal |Exif GPS latitude degrees        |35.0                    |
|GPSLatMin                |Decimal |Exif GPS latitude minutes        |39.0                    |
|GPSLatSec                |Decimal |Exif GPS latitude seconds        |30.89                   |
|GPSLatRef                |String  |Exif GPS latitude reference      |N: North<br>S: South    |
|GPSLon                   |Decimal |Exif GPS longitude degrees east  |139.745433              |
|GPSLonDeg                |Decimal |Exif GPS longitude degrees       |139.0                   |
|GPSLonMin                |Decimal |Exif GPS longitude minutes       |44.0                    |
|GPSLonSec                |Decimal |Exif GPS longitude seconds       |43.558                  |
|GPSLonRef                |String  |Exif GPS longitude reference     |E: East<br>W: West      |
|GPSAlt                   |Decimal |Exif GPS altitude (m)            |18.4                    |
|GPSAltRef                |String  |Exif GPS altitude reference      |0: Above sea level<br>1: Below sea level |

##### Format variable values
You can format variable values with '%' separator, like '${FNumber%0.0}'.

###### Format Integer or Decimal type value
|Symbol|Location           |Meaning                                        |
|------|-------------------|-----------------------------------------------|
|0     |Number             |Digit                                          |
|#     |Number             |Digit, zero shows as absent                    |
|.     |Number             |Decimal separator                              |
|,     |Number             |Grouping separator                             |
|;     |Subpattern boundary|Separates positive and negative subpatterns    |
|%     |Prefix or suffix   |Multiply by 100 and show as percentage         |
See more information: https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html

    e.g.)
    "${FNumber%0.0}"
    -> "8.0", "16.0"
    
    "${GPSLatDeg%0}°${GPSLatMin%0}'${GPSLatSec%0.0#} ${GPSLatRef}"
    -> "35°39'30.89 N"

###### Format Date type value
|Symbol|Meaning                    |Examples                                       |
|------|---------------------------|-----------------------------------------------|
|G     |era                        |AD; Anno Domini; A                             |
|u     |year                       |2004; 04                                       |
|y     |year-of-era                |2004; 04                                       |
|D     |day-of-year                |189                                            |
|M/L   |month-of-year              |7; 07; Jul; July; J                            |
|d     |day-of-month               |10                                             |
|Q/q   |quarter-of-year            |3; 03; Q3; 3rd quarter                         |
|Y     |week-based-year            |1996; 96                                       |
|w     |week-of-week-based-year    |27                                             |
|W     |week-of-month              |4                                              |
|E     |day-of-week                |Tue; Tuesday; T                                |
|e/c   |localized day-of-week      |2; 02; Tue; Tuesday; T                         |
|F     |week-of-month              |3                                              |
|a     |am-pm-of-day               |PM                                             |
|h     |clock-hour-of-am-pm (1-12) |12                                             |
|K     |hour-of-am-pm (0-11)       |0                                              |
|k     |clock-hour-of-am-pm (1-24) |0                                              |
|H     |hour-of-day (0-23)         |0                                              |
|m     |minute-of-hour             |30                                             |
|s     |second-of-minute           |55                                             |
|S     |fraction-of-second         |978                                            |
|A     |milli-of-day               |1234                                           |
|n     |nano-of-second             |987654321                                      |
|N     |nano-of-day                |1234000000                                     |
|V     |time-zone ID               |America/Los_Angeles; Z; -08:30                 |
|z     |time-zone name             |Pacific Standard Time; PST                     |
|O     |localized zone-offset      |GMT+8; GMT+08:00; UTC-08:00;                   |
|X     |zone-offset 'Z' for zero   |Z; -08; -0830; -08:30; -083015; -08:30:15;     |
|x     |zone-offset                |+0000; -08; -0830; -08:30; -083015; -08:30:15; |
|Z     |zone-offset                |+0000; -0800; -08:00;                          |
See more information: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

    e.g.)
    "${PhotoTakenDate%uuuu/MMdd}"
    -> "2012/0123"

##### Convert variable values
You can convert variables value with '/' and ':' separators, like '${WhiteBalance/0:Auto/1:Manual}'.

    / <Expression> : <Returned value>

If multiple '/' separator is specified, the first matching value is returned.
When it does not match any expression, then it returns an empty value.

    e.g.)
    "${WhiteBalance/0:Auto/1:Manual}"
    -> "Auto" or "Manual" or "" (When not matching)

The following operators can be specified as comparison conditions in the expression.

|Operator|Meaning                    |Variable Type                 |Examples                                                                     |
|--------|---------------------------|------------------------------|-----------------------------------------------------------------------------|
|=       |Equals  (Default)          |String, Integer, Decimal, Date|/=str:<br>/str:<br>/'str':<br>/123.456:<br>/#2012-01-23#:                    |
|=*      |Wildcard matches           |String                        |/=*'*str': (Ends with)<br>/=*'str*': (Starts with)<br>/=*'*str*': (Contains) |
|=~      |Regular expression matches |String                        |/=~'str[0-9]{4}':                                                            |
|!=      |Not equals                 |String, Integer, Decimal, Date|/!=str:<br>/!='str':<br>/!=123.456:<br>/!=#2012-01-23#:                      |
|<       |Less than                  |Integer, Decimal, Date        |/<123.456: <br> /<#2012-01-23#:                                              |
|<=      |Less than or equals        |Integer, Decimal, Date        |/<=123.456: <br> /<=#2012-01-23#:                                            |
|>       |Grater than                |Integer, Decimal, Date        |/>123.456: <br> />#2012-01-23#:                                              |
|>=      |Grater than or equals      |Integer, Decimal, Date        |/>=123.456: <br> />=#2012-01-23#:                                            |

If 'default' label is specified as the expression, it will match all values.

    e.g.)
    "${Make/=*'NIKON*':Nikon/'Canon':Canon/default:Others}"
    -> "Nikon" or "Canon" or "Others"


#### If already file exists ...
Specify processing when file of the same name exists.

- Confirm -- Display a confirmation dialog for each file
- Overwrite -- Overwrite a file
- Skip -- Skip processing for a file
- Terminate -- Terminate processing

#### Validate file - Check file digest.
It checks whether the contents of the file were correctly copied.
If you set an option to change the file, such as removing the exif of the file, it will not be checked.


### Change File Date

#### Change ...
Date of change target.

- Creation Date -- Change file creation date.
- Modified Date -- Change file modified date.
- Access Date -- Change file access date.
- Exif Date -- Change file exif date.

#### by ...
Date to set.

- Current Date -- Change by current date.
- File Creation Date -- Change by file creation date.
- File Modified Date -- Change by file modified date.
- File Access Date -- Change by file access date.
- File Exif Date -- Change by file exif date.
- Specific Date -- Change by specific date. You can input it.

#### with ...
Difference of set date.

- None
- Plus
- Minus
- Overwrite


### Change Exif
- Remove GPS exif tags.
- Remove ALL exif tags.


### Others

#### Dry Run
Simurate process.


## License
Picto is open-sourced software licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


## Repository
Picto is developed and managed on [GitHub](https://github.com/mozq/picto).


## Legal
Picto is built using open source software.

### Apache Commons Imaging (Sanselan)
> Apache Sanselan
> Copyright 2007-2009 The Apache Software Foundation.
> 
> This product includes software developed at
> The Apache Software Foundation (http://www.apache.org/).

### mifmi-commons4j
> The MIT License (MIT)
> 
> Copyright (c) 2015 mifmi.org and other contributors
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in all
> copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.
