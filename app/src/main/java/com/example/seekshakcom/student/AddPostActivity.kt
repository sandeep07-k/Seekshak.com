
package com.example.seekshakcom.student


import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.seekshakcom.R
import com.example.seekshakcom.model.PostRequest
import com.example.seekshakcom.network.ApiClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar


class AddPostActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var classEditText: AutoCompleteTextView
    private lateinit var subjectEditText: AutoCompleteTextView
    private lateinit var eduBoardEditText: AutoCompleteTextView
    private lateinit var locationEditText: AutoCompleteTextView
    private lateinit var fetchLocationButton: TextView
    private lateinit var feeEditText: EditText
    private lateinit var feeTypeRadioGroup: RadioGroup
    private lateinit var classScheduleEditText: AutoCompleteTextView
    private lateinit var classTimingEditText: AutoCompleteTextView
    private lateinit var genderSpinner: AutoCompleteTextView
    private lateinit var demoClassDateEditText: EditText
    private lateinit var modeOfClassesSpinner: AutoCompleteTextView
    private lateinit var minQualificationEditText: AutoCompleteTextView
    private lateinit var specialRequirementEditText: EditText
    private lateinit var submitButton: Button
    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var selectedSublocality: String? = null
    private var selectedArea: String? = null
    private var selectedCity: String? = null
    private var selectedState: String? = null
    private var selectedCountry: String? = null
    private lateinit var locationResultLauncher: ActivityResultLauncher<Intent>








    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Initialize views
        backArrow = findViewById(R.id.backArrow)
        classEditText = findViewById(R.id.classEditText)
        subjectEditText = findViewById(R.id.subjectEditText)
        locationEditText = findViewById(R.id.locationEditText)
        fetchLocationButton = findViewById(R.id.fetchLocationButton)
        eduBoardEditText = findViewById(R.id.edu_board)
        feeEditText = findViewById(R.id.feeEditText)
        feeTypeRadioGroup = findViewById(R.id.feeTypeRadioGroup)
        classScheduleEditText = findViewById(R.id.classScheduleEditText)
        classTimingEditText = findViewById(R.id.classTimingEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        demoClassDateEditText = findViewById(R.id.demoClassDateEditText)
        modeOfClassesSpinner = findViewById(R.id.modeOfClassesSpinner)
        minQualificationEditText = findViewById(R.id.minQualificationEditText)
        specialRequirementEditText = findViewById(R.id.specialRequirementEditText)
        submitButton = findViewById(R.id.submitButton)



        setupClassSuggestions()
        setupSubjectSuggestions()
        setupBoardSuggestions()
        setupGenderSpinner()
        setupModeOfClassesSpinner()
        setupclassScheduleDropdown()
        setupClassTimingDropdown()
        setupQualificationSuggestions()
        setupCalendarPicker()
        setupSpecialRequirementSuggestions()

        locationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data!!
                selectedLat = data?.getStringExtra("lat")?.toDoubleOrNull()
                selectedLon = data?.getStringExtra("lon")?.toDoubleOrNull()
                selectedSublocality = data?.getStringExtra("selected_sublocality")
                selectedArea = data?.getStringExtra("selected_area")
                selectedCity = data?.getStringExtra("selected_city")
                selectedState = data?.getStringExtra("selected_state")
                selectedCountry = data?.getStringExtra("selected_country")
//


                val locationText = listOfNotNull(selectedSublocality, selectedArea, selectedCity)
                    .joinToString(", ")

                locationEditText.setText(locationText)
            } else {
                Log.d("AutoFillCheck", "Result not OK or data is null")
            }
        }








        backArrow.setOnClickListener {
            finish()
        }
        fetchLocationButton.setOnClickListener {
            val intent = Intent(this, PostLocationSelectActivity::class.java)
            locationResultLauncher.launch(intent)

        }


        submitButton.setOnClickListener {
            submitForm()
        }
    }
    private fun setupClassSuggestions() {
        val classOptions = listOf(
            "LKG", "UKG",
            "Class 1", "Class 2", "Class 3", "Class 4", "Class 5",
            "Class 6", "Class 7", "Class 8", "Class 9", "Class 10",
            "Class 11 (Science)", "Class 11 (Commerce)", "Class 11 (Arts)",
            "Class 12 (Science)", "Class 12 (Commerce)", "Class 12 (Arts)",
            "Diploma", "BCA", "B.Sc", "B.Com", "BA",
            "MCA", "M.Sc", "M.Com", "MA", "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, classOptions)
        classEditText.setAdapter(adapter)
    }
    private fun setupSubjectSuggestions() {
        val subjects = listOf(
            "All main subjects", "Mathematics","Science", "Social Science","English", "Hindi",
            "Physics", "Chemistry", "Biology", "Social Studies", "History", "Geography", "Civics",
            "Economics (Basic)", "Computer Science",

            "Drawing", "Computer Basics", "Storytelling", "Handwriting Improvement",
            "Spoken English", "Hindi Grammar",
            "Sanskrit", "Moral Education", "Environmental Studies (EVS)", "General Knowledge",


            )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjects)
        subjectEditText.setAdapter(adapter)
    }
    private fun setupBoardSuggestions() {
        val boards = listOf(
            "CBSE",
            "BSEB",
            "ICSE",
            "State Board",
            "UP Board",
            "Bihar Board",
            "Rajasthan Board",
            "MP Board",
            "WB Board",
            "Punjab Board",
            "Maharashtra Board",
            "Karnataka Board",
            "Tamil Nadu Board",
            "Kerala Board",
            "Gujarat Board",
            "Telangana Board",
            "Andhra Pradesh Board",
            "Odisha Board",
            "Haryana Board",
            "Jharkhand Board",
            "Chhattisgarh Board",
            "Assam Board",
            "NIOS (National Institute of Open Schooling)",
            "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, boards)
        eduBoardEditText.setAdapter(adapter)
    }

    private fun setupclassScheduleDropdown() {
        val classDayOptions = listOf(
            "Mon to Sat",
            "Mon-Wed-Fri (MWF)",
            "Tue-Thu-Sat (TTS)",
            "Mon to Fri",
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun",
            "Mon-Wed", "Tue-Thu", "Wed-Fri", "Thu-Sat", "Sat-Sun",
            "Daily (7 Days)",
            "Alternate Days",
            "Weekends Only",
            "Custom Days"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,classDayOptions)
        classScheduleEditText.setAdapter(adapter)

    }
    private fun setupClassTimingDropdown() {
        val timingOptions = listOf(
            "7 AM – 8 AM ",
            "8 AM – 9 AM",
            "9 AM – 10 AM",
            "10 AM – 11 AM",
            "11 AM – 12 PM",
            "12 PM – 1 PM",
            "1 PM – 2 PM",
            "2 PM – 3 PM",
            "3 PM – 4 PM",
            "4 PM – 5 PM",
            "5 PM – 6 PM",
            "6 PM – 7 PM",
            "7 PM – 8 PM",
            "8 PM – 9 PM",
            "7 AM – 9 AM",
            "9 AM – 11 AM",
            "11 AM – 1 PM",
            "1 PM – 3 PM",
            "3 PM – 5 PM",
            "5 PM – 7 PM",
            "7 PM – 9 PM",
            "Morning Shift",
            "Evening Shift",
            "Flexible Timing",
            "Custom Timing"
        )

        val timingAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            timingOptions
        )

        classTimingEditText.setAdapter(timingAdapter)

    }





    private fun setupGenderSpinner() {
        val genderOptions = listOf(
            "Male or Female (any)",
            "Male Tutor Only",
            "Female Tutor Only"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        genderSpinner.setAdapter(adapter)
//        genderSpinner.setText("Any Available tutor", true)
    }

    private fun setupModeOfClassesSpinner() {
        val modeOptions = listOf(
            "Offline (In-person at Home)",
            "Online (Video Call Based)",
            "Hybrid (Both Online and Offline)"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modeOptions)
        modeOfClassesSpinner.setAdapter(adapter)
        modeOfClassesSpinner.setText("Offline (In-person at Home)",true)
    }
    private fun setupQualificationSuggestions() {
        val qualificationOptions = listOf(
            "Any Available tutor","10th Pass", "12th Pass", "Graduate", "Postgraduate",
            "B.Ed", "M.Ed", "Ph.D", "Diploma Holder", "Professional Degree", "Not Required"
        )

        val qualificationAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            qualificationOptions
        )

        minQualificationEditText.setAdapter(qualificationAdapter)
    }


    private fun setupCalendarPicker() {
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = "${dayOfMonth}/${month + 1}/${year}"
            demoClassDateEditText.setText(selectedDate)
        }

        demoClassDateEditText.setOnClickListener {
            showDatePicker(calendar, dateSetListener)
        }
    }

    private fun showDatePicker(calendar: Calendar, listener: DatePickerDialog.OnDateSetListener) {
        val datePickerDialog = DatePickerDialog(
            this,
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }
    private fun setupSpecialRequirementSuggestions() {
        val specialRequirementSuggestions = listOf(
            "Good communication skills",
            "Experienced with slow learners",
            "Comfortable with online teaching",
            "Knows phonics & early reading",
            "ICSE background preferred",
            "Female tutor preferred",
            "Weekend classes only",
            "Experienced with board exam prep",
            "Can teach multiple subjects",
            "Comfortable teaching in Hindi",
            "Comfortable teaching in English"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            specialRequirementSuggestions
        )

        (specialRequirementEditText as? AutoCompleteTextView)?.setAdapter(adapter)
    }
    private fun isValidFeeInput(feeInput: String): Boolean {
        val trimmedInput = feeInput.trim()

        // Regex to match either:
        // - A single number (e.g. 500)
        // - A range with two numbers, allowing - or – as separator (e.g. 500-1000 or 500–1000)
        val feePattern = Regex("^\\d{2,5}(?:[-–]\\d{2,5})?$")

        return feePattern.matches(trimmedInput)
    }



    private fun submitForm() {
        val className = classEditText.text.toString().trim()
        val subject = subjectEditText.text.toString().trim()
        val educationBoard = eduBoardEditText.text.toString().trim()
        val feeAmount = feeEditText.text.toString().trim()
        val feeType = when (feeTypeRadioGroup.checkedRadioButtonId) {
            R.id.radioHourly -> "hourly"
            R.id.radioMonthly -> "monthly"
            else -> ""
        }
        val classSchedule = classScheduleEditText.text.toString().trim()
        val classTiming = classTimingEditText.text.toString().trim()
        val gender = genderSpinner.text.toString()
        val demoClassDate = demoClassDateEditText.text.toString().trim()
        val modeOfClass = modeOfClassesSpinner.text.toString()
        val qualification = minQualificationEditText.text.toString().trim()
        val specialReq = specialRequirementEditText.text.toString().trim()

        val rootView = findViewById<View>(R.id.rootLayout) ?: return

        if (className.isEmpty()) { classEditText.error = "Please enter class"; classEditText.requestFocus(); return }
        if (subject.isEmpty()) { subjectEditText.error = "Please enter subjects"; subjectEditText.requestFocus(); return }
        if (educationBoard.isEmpty()) { eduBoardEditText.error = "Enter education board"; eduBoardEditText.requestFocus(); return }
        if (selectedLat == null || selectedLon == null ||
            selectedCity.isNullOrEmpty() || selectedState.isNullOrEmpty() || selectedCountry.isNullOrEmpty()
        ) {
            Snackbar.make(rootView, "Please select a valid location", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (feeAmount.isEmpty() || feeType.isEmpty()) { feeEditText.error = "Enter fee and select type"; feeEditText.requestFocus(); return }
        if (classSchedule.isEmpty()) { classScheduleEditText.error = "Enter days"; classScheduleEditText.requestFocus(); return }
        if (gender.isEmpty()) { Snackbar.make(rootView, "Please select preferred gender", Snackbar.LENGTH_SHORT).show(); return }
        if (modeOfClass.isEmpty()) { Snackbar.make(rootView, "Please select mode of class", Snackbar.LENGTH_SHORT).show(); return }
        if (qualification.isEmpty()) { minQualificationEditText.error = "Enter minimum qualification"; minQualificationEditText.requestFocus(); return }
        if (specialReq.length > 200) { specialRequirementEditText.error = "Too long – max 200 characters"; specialRequirementEditText.requestFocus(); return }

        val feeInput = feeEditText.text.toString().trim()

        if (!isValidFeeInput(feeInput)) {
            feeEditText.error = "Enter a valid fee or range (e.g. 500 or 500–1000)"
            return
        }

        val fee = "₹${feeInput}/${if (feeType == "hourly") "hr" else "month"}"


        val post = PostRequest(
            className,
            subject,
            educationBoard,
            fee,
            classSchedule,
            classTiming,
            gender,
            demoClassDate,
            modeOfClass,
            qualification,
            specialReq,
            latitude = selectedLat?: 0.0,
            longitude = selectedLon ?: 0.0,
            sublocality = selectedSublocality ?: "",
            area = selectedArea ?: "",
            city = selectedCity ?: "",
            state = selectedState ?: "",
            country = selectedCountry ?: ""


        )
        val dialogView = layoutInflater.inflate(R.layout.dialog_submit, null)
        val loadingDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        loadingDialog.show()


        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Snackbar.make(rootView, "You're not logged in", Snackbar.LENGTH_LONG).show()
            loadingDialog.dismiss()
            return
        }

        submitButton.isEnabled = false
        submitButton.animate().alpha(0.5f).setDuration(300).start()


        user.getIdToken(true)
            .addOnSuccessListener { result ->
                val token = result.token
                if (token.isNullOrEmpty()) {

                    Snackbar.make(rootView, "Token generation failed", Snackbar.LENGTH_LONG).show()
                    submitButton.animate().alpha(1f).setDuration(300).withEndAction {
                        submitButton.isEnabled = true
                        loadingDialog.dismiss()
                    }.start()
                    return@addOnSuccessListener
                }

                lifecycleScope.launch {
                    try {
                        val response = ApiClient.instance.addPost("Bearer $token", post)
                        loadingDialog.dismiss()


                        if (response.isSuccessful) {
                            AlertDialog.Builder(this@AddPostActivity)
                                .setTitle("Success")
                                .setMessage("Post submitted successfully!")
                                .setPositiveButton("OK") { _, _ ->
                                    finish() // finish activity after OK
                                }
                                .show()

                            clearFormFields()
                            submitButton.animate().alpha(0.5f).setDuration(300).start()
                            submitButton.isEnabled = false
                        } else {
                            AlertDialog.Builder(this@AddPostActivity)
                                .setTitle("Error")
                                .setMessage("Something went wrong. Code: ${response.code()}")
                                .setPositiveButton("OK", null)
                                .show()

                            submitButton.animate().alpha(1f).setDuration(300).withEndAction {
                                submitButton.isEnabled = true
                            }.start()
                        }
                    } catch (e: Exception) {
                        loadingDialog.dismiss()
                        AlertDialog.Builder(this@AddPostActivity)
                            .setTitle("Network Error")
                            .setMessage("Something went wrong: ${e.localizedMessage}")
                            .setPositiveButton("OK", null)
                            .show()

                        submitButton.animate().alpha(1f).setDuration(300).withEndAction {
                            submitButton.isEnabled = true
                        }.start()
                    }
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                AlertDialog.Builder(this@AddPostActivity)
                    .setTitle("Authentication Failed")
                    .setMessage("Something went wrong: ${it.message}")
                    .setPositiveButton("OK", null)
                    .show()

                submitButton.animate().alpha(1f).setDuration(300).withEndAction {
                    submitButton.isEnabled = true
                }.start()
            }
    }

    private fun clearFormFields() {
        classEditText.text.clear()
        subjectEditText.text.clear()
        eduBoardEditText.text.clear()
        locationEditText.text.clear()
        feeEditText.text.clear()
        classScheduleEditText.text.clear()
        demoClassDateEditText.text.clear()
        minQualificationEditText.text.clear()
        specialRequirementEditText.text.clear()
        genderSpinner.setText("", false)
        modeOfClassesSpinner.setText("", false)
        feeTypeRadioGroup.clearCheck()
    }
}
