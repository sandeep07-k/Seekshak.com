
package com.example.seekshakcom

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.model.MyPost
import com.example.seekshakcom.model.PostRequest
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.network.ApiResponse
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class EditPostActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var classEditText: AutoCompleteTextView
    private lateinit var subjectEditText: AutoCompleteTextView
    private lateinit var eduBoardEditText: AutoCompleteTextView
    private lateinit var feeEditText: TextInputEditText
    private lateinit var feeTypeRadioGroup: RadioGroup
    private lateinit var durationEditText: AutoCompleteTextView
    private lateinit var classScheduleEditText: AutoCompleteTextView
    private lateinit var classTimingEditText: AutoCompleteTextView
    private lateinit var genderSpinner: AutoCompleteTextView
    private lateinit var demoClassDateEditText: TextInputEditText
    private lateinit var modeOfClassesSpinner: AutoCompleteTextView
    private lateinit var minQualificationEditText: AutoCompleteTextView
    private lateinit var specialRequirementEditText: MaterialAutoCompleteTextView
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar

    private var postId: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Init Views
        backArrow = findViewById(R.id.backArrow)
        classEditText = findViewById(R.id.classEditText)
        subjectEditText = findViewById(R.id.subjectEditText)
        eduBoardEditText = findViewById(R.id.edu_board)
        feeEditText = findViewById(R.id.feeEditText)
        feeTypeRadioGroup = findViewById(R.id.feeTypeRadioGroup)
        durationEditText = findViewById(R.id.durationEditText)
        classScheduleEditText = findViewById(R.id.classScheduleEditText)
        classTimingEditText = findViewById(R.id.classTimingEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        demoClassDateEditText = findViewById(R.id.demoClassDateEditText)
        modeOfClassesSpinner = findViewById(R.id.modeOfClassesSpinner)
        minQualificationEditText = findViewById(R.id.minQualificationEditText)
        specialRequirementEditText = findViewById(R.id.specialRequirementEditText)
        submitButton = findViewById(R.id.submitButton)
        progressBar = findViewById(R.id.progressBar)

        backArrow.setOnClickListener { finish() }
        demoClassDateEditText.setOnClickListener { showDatePicker() }

        setupSuggestions()

        val post = intent.getSerializableExtra("POST_DATA", MyPost::class.java)
        post?.let { populateFields(it) }

        submitButton.setOnClickListener { showConfirmationDialog() }
    }

    private fun populateFields(post: MyPost) {
        postId = post._id
        classEditText.setText(post.className)
        subjectEditText.setText(post.subject)
        eduBoardEditText.setText(post.educationBoard)
        feeEditText.setText(post.fee.filter { it.isDigit() })
        durationEditText.setText(post.duration)
        classScheduleEditText.setText(post.classSchedule)
        classTimingEditText.setText(post.classTiming)
        genderSpinner.setText(post.gender, false)
        demoClassDateEditText.setText(post.demoClassDate)
        modeOfClassesSpinner.setText(post.modeOfClass, false)
        minQualificationEditText.setText(post.qualification)
        specialRequirementEditText.setText(post.specialReq)

        when {
            post.fee.contains("hour", true) -> feeTypeRadioGroup.check(R.id.radioHourly)
            post.fee.contains("month", true) -> feeTypeRadioGroup.check(R.id.radioMonthly)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            demoClassDateEditText.setText("$day/${month + 1}/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }

    private fun setupSuggestions() {
        val context = this

        classEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "LKG", "UKG", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5",
            "Class 6", "Class 7", "Class 8", "Class 9", "Class 10",
            "Class 11 (Science)", "Class 11 (Commerce)", "Class 11 (Arts)",
            "Class 12 (Science)", "Class 12 (Commerce)", "Class 12 (Arts)",
            "Diploma", "BCA", "B.Sc", "B.Com", "BA", "MCA", "M.Sc", "M.Com", "MA", "Other"
        )))

        subjectEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "All Subjects", "Mathematics", "Science", "Social Science", "English", "Hindi", "Physics", "Chemistry",
            "Biology", "History", "Geography", "Civics", "Economics (Basic)", "Computer Science",
            "Drawing", "Computer Basics", "Storytelling", "Handwriting Improvement",
            "Spoken English", "Hindi Grammar", "Sanskrit", "Moral Education", "EVS", "General Knowledge"
        )))

        eduBoardEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "CBSE", "BSEB", "ICSE", "State Board", "UP Board", "Bihar Board", "Rajasthan Board", "MP Board",
            "WB Board", "Punjab Board", "Maharashtra Board", "Karnataka Board", "Tamil Nadu Board",
            "Kerala Board", "Gujarat Board", "Telangana Board", "Andhra Pradesh Board", "Odisha Board",
            "Haryana Board", "Jharkhand Board", "Chhattisgarh Board", "Assam Board", "NIOS", "Other"
        )))

        durationEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "45 min", "0.5 hour", "1 hour", "1.5 hours", "2 hours", "2.5 hours", "3 hours"
        )))

        classScheduleEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "Mon-Wed-Fri (MWF)", "Tue-Thu-Sat (TTS)", "Mon to Fri", "Mon to Sat", "Weekends Only", "Daily (7 Days)",
            "Alternate Days", "Only Mondays", "Custom Days"
        )))

        classTimingEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "7 AM – 8 AM", "8 AM – 9 AM", "9 AM – 10 AM", "10 AM – 11 AM", "11 AM – 12 PM", "12 PM – 1 PM",
            "1 PM – 2 PM", "2 PM – 3 PM", "3 PM – 4 PM", "4 PM – 5 PM", "5 PM – 6 PM", "6 PM – 7 PM",
            "7 PM – 8 PM", "8 PM – 9 PM", "Flexible Timing", "Custom Timing"
        )))

        genderSpinner.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "Male Tutor Only", "Female Tutor Only", "Any Available Tutor"
        )))

        modeOfClassesSpinner.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "Offline (In-person at Home)", "Online (Video Call Based)", "Hybrid (Both Online and Offline)"
        )))

        minQualificationEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "Any Available tutor", "10th Pass", "12th Pass", "Graduate", "Postgraduate",
            "B.Ed", "M.Ed", "Ph.D", "Diploma Holder", "Professional Degree", "Not Required"
        )))

        specialRequirementEditText.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listOf(
            "Good communication skills", "Experienced with slow learners", "Comfortable with online teaching",
            "Knows phonics & early reading", "ICSE background preferred", "Female tutor preferred",
            "Weekend classes only", "Experienced with board exam prep", "Can teach multiple subjects",
            "Comfortable teaching in Hindi", "Comfortable teaching in English"
        )))
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Update")
            .setMessage("Are you sure you want to update this post?")
            .setPositiveButton("Yes") { _, _ -> updatePost() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePost() {
        val className = classEditText.text.toString().trim()
        val subject = subjectEditText.text.toString().trim()
        val educationBoard = eduBoardEditText.text.toString().trim()
        val feeAmount = feeEditText.text.toString().trim()
        val feeType = when (feeTypeRadioGroup.checkedRadioButtonId) {
            R.id.radioHourly -> "hourly"
            R.id.radioMonthly -> "monthly"
            else -> ""
        }
        val duration = durationEditText.text.toString().trim()
        val classSchedule = classScheduleEditText.text.toString().trim()
        val classTiming = classTimingEditText.text.toString().trim()
        val gender = genderSpinner.text.toString().trim()
        val demoClassDate = demoClassDateEditText.text.toString().trim()
        val modeOfClass = modeOfClassesSpinner.text.toString().trim()
        val qualification = minQualificationEditText.text.toString().trim()
        val specialReq = specialRequirementEditText.text.toString().trim()

        if (className.isEmpty() || subject.isEmpty() || educationBoard.isEmpty() ||
            feeAmount.isEmpty() || feeType.isEmpty() || duration.isEmpty() ||
            classSchedule.isEmpty() || gender.isEmpty() || modeOfClass.isEmpty() || qualification.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val fee = "₹$feeAmount/${if (feeType == "hourly") "hr" else "month"}"
        val updatedPost = PostRequest(
            className, subject, educationBoard, fee, duration, classSchedule,
            classTiming, gender, demoClassDate, modeOfClass, qualification, specialReq
        )

        progressBar.visibility = View.VISIBLE
        submitButton.isEnabled = false

        ApiClient.instance.updatePost(postId!!, updatedPost)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    progressBar.visibility = View.GONE
                    submitButton.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@EditPostActivity, "Post updated successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish() // 👈 This will auto-refresh MyPostsFragment if handled
                    } else {
                        Toast.makeText(this@EditPostActivity, "Failed to update post", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    submitButton.isEnabled = true
                    Toast.makeText(this@EditPostActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
