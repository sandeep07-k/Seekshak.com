# SeekShak.com

SeekShak.com is a location-based tutoring mobile application built with **Android (Kotlin)** and **Node.js + MongoDB** backend.  
It helps students find nearby tutors using real-time location and allows tutors to create, edit, and manage posts easily.

---

## 🚀 Features
- Firebase OTP-based phone authentication
- Email verification using Node.js + Nodemailer
- User profile with image upload, crop (uCrop), and update functionality
- Location-based tutoring posts using MongoDB GeoJSON
- View nearby tutors with sorting and filtering
- Retrofit + Glide integration for networking and image loading
- SharedPreferences for local user data storage
- Node.js backend with Express, Multer, and MongoDB

---

## 🧰 Tech Stack
**Frontend (Android):**
- Kotlin
- Firebase Authentication
- Retrofit
- Glide
- UCrop
- SharedPreferences

**Backend:**
- Node.js + Express
- MongoDB + Mongoose
- Multer (image upload)
- Nodemailer (email OTP)
- Cloudinary (optional for image hosting)

---

## 📱 Screens (suggested)
- Login / OTP Verification
- Profile
- Create Post
- Nearby Tutors Feed
- Edit Post

---

## ⚙️ Installation
### Android App
1. Clone the repo
2. Add your Firebase `google-services.json`
3. Update `BASE_URL` in Retrofit client
4. Run on Android Studio

### Backend
1. `npm install`
2. Add `.env` file with:
