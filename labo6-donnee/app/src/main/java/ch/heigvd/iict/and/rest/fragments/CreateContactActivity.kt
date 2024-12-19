package ch.heigvd.iict.and.rest.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ch.heigvd.iict.and.rest.databinding.ActivityCreateContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import androidx.activity.viewModels
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory
import java.util.Calendar

class CreateContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateContactBinding
    private val contactsViewModel: ContactsViewModel by viewModels {
        ContactsViewModelFactory((application as ContactsApplication).repository)
    }
    private var birthday: Calendar? = null // Store the selected birthday

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure the Spinner for PhoneType
        val phoneTypes = PhoneType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, phoneTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.type.adapter = adapter

        // Configure Birthday Picker
        binding.birthdayPicker.setOnClickListener {
            showDatePicker()
        }

        // Save button logic
        binding.saveButton.setOnClickListener {
            saveContact()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Update the birthday field
            birthday = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            binding.birthday.text = "${selectedDay}/${selectedMonth + 1}/${selectedYear}" // Format date as DD/MM/YYYY
        }, year, month, day)

        datePicker.show()
    }

    private fun saveContact() {
        // Retrieve user input
        val name = binding.name.text.toString()
        val firstname = binding.firstname.text.toString()
        val email = binding.email.text.toString()
        val address = binding.address.text.toString()
        val zip = binding.zip.text.toString()
        val city = binding.city.text.toString()
        val phoneType = PhoneType.valueOf(binding.type.selectedItem.toString())
        val phoneNumber = binding.phoneNumber.text.toString()

        // Validate required fields
        if (name.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Name and Phone Number are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new Contact object
        val newContact = Contact(
            name = name,
            firstname = firstname,
            birthday = birthday,
            email = email,
            address = address,
            zip = zip,
            city = city,
            type = phoneType,
            phoneNumber = phoneNumber,
            syncStatus = false // New contact is unsynchronized
        )

        // Save contact using ViewModel
        contactsViewModel.insert(newContact)
        Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show()
        finish() // Close the activity
    }
}

