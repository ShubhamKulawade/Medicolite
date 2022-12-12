# TODOS

- [ ] save validation on doctor timing  
- [ ] save validation on doctor profile  
- [X] Appointment format in db "date Time"  
- [X] Add km in distance and also reduce the decimal to 2 point instead of 8 or 7  
- [X] Add fcm in userMainScreen and DoctorMainScreen;  
- [X] Greetings on main page  
~~- [ ] Logic to remove cancel button on time passed (Cancelled)~~  
- [X] Forgot Password  
- [X] Orientation (IMP)  
~~- [ ] Change Fcm Logic (Cancelled)~~  
~~- [ ] change online presence logic (Cancelled)~~  
- [X] removed sort button from user main screen  
- [X] Added sort button to Doctor main screen and appointments screen  
- [ ] set Dialogs as not cancellable  
- [X] password should contains special character  
- [X] Send Verification  
- [X] Change sign in logic to check if user is verified  
- [X] Add sort button to view all in user main  
- [X] Add Machine learning module.  
- [X] Link Disease module from navigation  
- [X] Add prescription  
- [X] Add prescription history
- [X] Add scanned image History
- [X] Linked new google-Services.json (Imp)
- [X] Test Prescription module, Doctor navigation, User navigation, Doctor clicked on "Complete"
- [X] Gallery Image path
~~- [ ]Add tool Bar~~
- [x] Change App Icon
- [X] Appointment Limit for a User On single Day.
- [ ] Remove bypassed methods in splash and signing after testing (IMP)
- [ ] Add else in Validation (set editext.error to null) on Edittexts in Userprofile and DoctorProfile

## Errors

- [X] Doctor Profile time and day
- [X] DocumentUid in Appointment
- [X] Key in Appointment (time part was null)
- [X] DoctorUid in Appointment
- [X] Logic in MainActivity to Redirect user based on authorization when auth!=null
- [X] link forgot Password text to activity.
- [ ] ImageView default image changes according to theme (if its light mode the img is fine else in dark mode the image is inverted) //update: -No idea
- [X] trying to save bitmap in sqLite (save image to device and pass the path to sqLite) //update: now saves image's uri instead of image itself
- [X] Patient name null in Add_Prescription from Doctor Screen

## Fixed

- [X] Implemented Prescription
- [X] Loading Dialog not dismissed in Prescription History
- [X] Fixed Prescription (fixed error in upload,fixed prescription data null)
- [X] User cant book same doctor Multiple time in same time frame
- [X] Creation TimeStamp Missing in Prescription
- [X] Added Clickable tag in list_prescription (for ripple effect)
