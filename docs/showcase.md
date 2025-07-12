# ARAAFTOR - Example of how the extension works

The ATAK application extension was written in an augmented reality system to enable the operator to continuously observe the battlefield.

**Estimating the distance to an object**

![Tactical situation](docs/est_dis.png)

The user GUI consists of three parts: the top and bottom bars, and the main section. The top bar is responsible for providing as much information as possible to the soldier on the battlefield. It includes information about the soldier's location, GPS signal quality, device battery status, time of day, temperature, and wind. Light intensity levels in lux (lx) are also included, along with the current date and time, located just below the bar. The bottom bar consists of a compass, azimuth indicator, altitude, and tilt indicator. On the left, next to the route icon, is the distance to the object in the image grid (the main part of the screen). This distance is calculated using the angular method using the grid size and the object's actual height, which is displayed to the user on the right side of the bottom bar. On the right side of the bar is also a button with a camera icon, which, when clicked, initiates the "Detect from Photo" use case.

**Real tactical situation in an augmented reality system**

![Tactical situation](docs/syt_takt.png)

The figure above shows how the tactical situation around the user is drawn.

**Orientation in the terrain and in the prevailing situation on the battlefield**

![Tactical situation](docs/ort_syt.png)

The image above shows the map settings window, which allows users to adjust its visibility and size. This modification allows users to change the map view to a compass view, showing the current direction the phone's camera is pointing. These views can be repositioned across the entire device screen and their transparency and size can be adjusted. 

**Detection model results change panel**

![Tactical situation](docs/pan_ch.png)

The appearance of the detection results modification panel for a specific photo assigned to a detection history item. The top panel bar contains a compass, which, through rotation, allows you to change the azimuth toward which the device was facing while taking the photo. This allows you to modify the position of the tactical marker on the mini map relative to the user's current location after making changes. Next, there are two buttons for zooming in and out of the photo, relative to the user's current location. Next, there are buttons for changing the selected bounding box in the photo, along with an editable name of the class to which the model assigned the detected element. Further on, on the right, are buttons for adding a new bounding box to the photo and deleting the selected bounding box. Below the top bar is the main panel view. On the left is a scroll bar for zooming in and out of the photo. In the center of the view, there is the photo with the bounding boxes marked on it. These boxes are marked with two colors: green indicates the user-selected bounding box, whose class name and pixel position values are displayed on the screen, while red indicates the bounding box available for modification. Each of them has a button in its upper left corner that, when pressed and held, allows the user to modify its height and width. The right side of the upper panel displays information about the percentage of certainty that the selected boungong box has been assigned to a given class and the pixel value of its position in the photo. Beneath this information is a button that allows the user to save or discard the changes.

**Information about the model detection results**
![Tactical situation](docs/res_det.png)

The top figure shows the information displayed in the right corner of the top bar about the model detection results.
