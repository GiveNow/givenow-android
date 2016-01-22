# GiveNow
### Cloning instructions
Make sure to grab the submodules!
```
git clone --recursive git@github.com:GiveNow/givenow-android.git
```
or 
```
git clone git@github.com:GiveNow/givenow-android.git
cd givenow-android
git submodule init
git submodule update
```
# What is GiveNow?
The GiveNow Android app is designed to make it easier for users to donate to donation centers, and to help organize volunteers.

Some of these screenshots still need to be updated. Bear with us!

## For Donors

(Users can login annonymously, this reduces friction to prevent dropoff in a signup flow.)

Donors can request a donation pickup from their house.

![Donor Request](/assets/donor_request.png?raw=true)

In the detailed view they can fine tune their location, and choose which items they want to donate.

![Donor detailed view](/assets/donor_request_detail.png?raw=true)![Donor category chooser](/assets/donor_choose_categories.png?raw=true)

The Donor needs to confirm their name and phone number so the volunteer can contact them.

![Donor Confirmation](/assets/donor_request_confirmation.png?raw=true)

The donation request has been submitted.  Now the donor is just awaiting a push notification telling them a volunteer is ready to pick up the donation.

![Donation Submitted](/assets/donor_request_submitted.png?raw=true)

When a volunteer accepts, the user is asked to confirm that the coats are available today.

![Donation Pickup Confirmed](/assets/donor_pickup_confirmation.png?raw=true)

## For Volunteers

A Volunteer can view all the Requested pickups in their area.

![Volunteer Pickups](/assets/volunteer_pickups.png?raw=true)

The volunteer can see the detailed information about the pickup, and then accept it.

![Volunteer Pickup details](/assets/volunteer_pickups_detail.png?raw=true)

The Volunteer can see their pending and confirmed pickups on their dashboard.

![Dashboard](/assets/dashboard_pickups.png?raw=true)

The Volunteer can now find and drive to a dropoff location.

![Dropoff Locations](/assets/dropoff_locations.png?raw=true)

## Profile Page

Donors can view their Donation History

![Donation History](/assets/profile_donation_history.png?raw=true)

Volunteers can view their Volunteer History

![Volunteer History](/assets/profile_volunteer_history.png?raw=true)
