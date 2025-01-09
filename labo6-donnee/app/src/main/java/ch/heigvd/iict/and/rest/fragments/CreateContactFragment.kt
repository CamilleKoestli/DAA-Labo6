package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentCreateContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateContactFragment : Fragment() {

    private var _binding: FragmentCreateContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactsViewModel
    private var selectedPhoneType: PhoneType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateContactBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ContactsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the title for creating a new contact
        activity?.title = getString(R.string.fragment_detail_title_new)

        // Setup spinner for PhoneType selection
        val phoneTypes = resources.getStringArray(R.array.contact_types)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            phoneTypes
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.type.adapter = spinnerAdapter

        binding.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPhoneType = PhoneType.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPhoneType = null
            }
        }

        // Save button click logic
        binding.saveButton.text = getString(R.string.fragment_btn_save)
        binding.saveButton.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val firstname = binding.firstname.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val address = binding.address.text.toString().trim()
            val zip = binding.zip.text.toString().trim()
            val city = binding.city.text.toString().trim()
            val phoneNumber = binding.phoneNumber.text.toString().trim()

            // Validate input
            if (name.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.name_title) + " " + getString(R.string.fragment_phonenumber_subtitle) + " " + getString(
                        R.string.fragment_btn_create
                    ),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }


// Convert birthday string to Calendar
            val birthdayString = binding.birthday.text.toString().takeIf { it.isNotEmpty() }
            val birthday: Calendar? = birthdayString?.let {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                Calendar.getInstance().apply {
                    time = sdf.parse(it)
                }
            }

// Create a new contact
            val newContact = Contact(
                id = null, // Let Room generate the ID
                name = name,
                firstname = firstname.ifEmpty { null },
                birthday = birthday,
                email = email.ifEmpty { null },
                address = address.ifEmpty { null },
                zip = zip.ifEmpty { null },
                city = city.ifEmpty { null },
                type = selectedPhoneType,
                phoneNumber = phoneNumber.ifEmpty { null },
                syncStatus = false // Default to unsynchronized
            )

            // Save the contact via ViewModel
            viewModel.insert(newContact)

            // Navigate back or close
            Toast.makeText(requireContext(), getString(R.string.new_contact), Toast.LENGTH_SHORT)
                .show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Cancel button logic
        binding.buttonCancel.text = getString(R.string.fragment_btn_cancel)
        binding.buttonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Birthday picker button logic
        binding.birthdayPicker.text = getString(R.string.birthday_title)
        binding.birthdayPicker.setOnClickListener {
            // TODO: Add logic to show a date picker dialog and set the birthday
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
