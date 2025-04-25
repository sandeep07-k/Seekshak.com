package com.example.seekshakcom

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var classEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var eduBoardEditText: EditText
    private lateinit var feeEditText: EditText
    private lateinit var durationEditText: EditText
    private lateinit var noOfClassesEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var demoClassDateEditText: EditText
    private lateinit var calendarIcon: ImageView
    private lateinit var modeOfClassesSpinner: Spinner
    private lateinit var minQualificationEditText: EditText
    private lateinit var specialRequirementEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        // Initialize Views
        backArrow = findViewById(R.id.backArrow)
        classEditText = findViewById(R.id.classEditText)
        subjectEditText = findViewById(R.id.subjectEditText)
        eduBoardEditText = findViewById(R.id.edu_board)
        feeEditText = findViewById(R.id.feeEditText)
        durationEditText = findViewById(R.id.durationEditText)
        noOfClassesEditText = findViewById(R.id.noOfClassesEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        demoClassDateEditText = findViewById(R.id.demoClassDateEditText)
        calendarIcon = findViewById(R.id.calendarIcon)
        modeOfClassesSpinner = findViewById(R.id.modeOfClassesSpinner)
        minQualificationEditText = findViewById(R.id.minQualificationEditText)
        specialRequirementEditText = findViewById(R.id.specialRequirementEditText)
        submitButton = findViewById(R.id.submitButton)

        setupGenderSpinner()
        setupModeOfClassesSpinner()
        setupCalendarPicker()

        backArrow.setOnClickListener {
            finish() // Go back to previous screen
        }

        submitButton.setOnClickListener {
            submitForm()
        }
    }

    private fun setupGenderSpinner() {
        val genderOptions = listOf("No Preference", "Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        genderSpinner.adapter = adapter
    }

    private fun setupModeOfClassesSpinner() {
        val modeOptions = listOf("Offline", "Online", "Both")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modeOptions)
        modeOfClassesSpinner.adapter = adapter
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

        calendarIcon.setOnClickListener {
            showDatePicker(calendar, dateSetListener)
        }
    }

    private fun showDatePicker(calendar: Calendar, listener: DatePickerDialog.OnDateSetListener) {
        DatePickerDialog(
            this,
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun submitForm() {
        // Collect the data from fields
        val className = classEditText.text.toString().trim()
        val subject = subjectEditText.text.toString().trim()
        val educationBoard = eduBoardEditText.text.toString().trim()
        val fee = feeEditText.text.toString().trim()
        val duration = durationEditText.text.toString().trim()
        val noOfClasses = noOfClassesEditText.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val demoClassDate = demoClassDateEditText.text.toString().trim()
        val modeOfClass = modeOfClassesSpinner.selectedItem.toString()
        val qualification = minQualificationEditText.text.toString().trim()
        val specialReq = specialRequirementEditText.text.toString().trim()

        // You can now send this data to your server or save locally
        Toast.makeText(this, "Form Submitted Successfully!", Toast.LENGTH_SHORT).show()
    }
}
