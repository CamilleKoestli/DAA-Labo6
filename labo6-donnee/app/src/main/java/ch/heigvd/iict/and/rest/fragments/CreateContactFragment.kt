/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This fragment provides a user interface for creating a new contact.
 *               It handles input validation, date selection, and interacts with the
 *               ViewModel to save the new contact to the database.
 */

package ch.heigvd.iict.and.rest.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentCreateContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.models.Status
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import java.util.*

class CreateContactFragment : Fragment() {
    private var _binding: FragmentCreateContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ContactsViewModel
    private var selectedPhoneType: PhoneType? = null
    private var selectedBirthday: Calendar? = null

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

        // Setup RadioGroup for PhoneType selection
        binding.radioGroupPhoneType.setOnCheckedChangeListener { _, checkedId ->
            selectedPhoneType = when (checkedId) {
                R.id.radio_home -> PhoneType.HOME
                R.id.radio_office -> PhoneType.OFFICE
                R.id.radio_mobile -> PhoneType.MOBILE
                R.id.radio_fax -> PhoneType.FAX
                else -> null
            }
        }

        // Open a date picker when clicking the Birthday field
        binding.birthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate =
                    String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.birthday.setText(selectedDate)
                selectedBirthday = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
            }, year, month, day).show()
        }

        // Save button click logic
        binding.createButton.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val phoneNumber = binding.phoneNumber.text.toString().trim()

            // Validate input
            if (name.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.fragment_name_subtitle) + " and " + getString(R.string.fragment_phonenumber_subtitle) + " are required.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Create a new contact
            val newContact = Contact(
                id = null,
                name = name,
                firstname = binding.firstname.text.toString().trim().ifEmpty { null },
                birthday = selectedBirthday?.toFormattedString(),
                email = binding.email.text.toString().trim().ifEmpty { null },
                address = binding.address.text.toString().trim().ifEmpty { null },
                zip = binding.zip.text.toString().trim().ifEmpty { null },
                city = binding.city.text.toString().trim().ifEmpty { null },
                type = selectedPhoneType,
                phoneNumber = phoneNumber.ifEmpty { null },
                status = Status.NEW
            )

            // Save the contact via ViewModel
            viewModel.insert(newContact) { insertedId ->
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Contact created with ID: $insertedId",
                        Toast.LENGTH_SHORT
                    ).show()
                    view?.post {
                        if (isAdded) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        }

        // Cancel button logic
        binding.cancelButton.text = getString(R.string.fragment_btn_cancel)
        binding.cancelButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    // Helper function to format Calendar to "dd/mm/yyyy"
    private fun Calendar?.toFormattedString(): String? {
        if (this == null) return null
        val day = get(Calendar.DAY_OF_MONTH)
        val month = get(Calendar.MONTH) + 1 // Les mois sont basés sur 0
        val year = get(Calendar.YEAR)
        return String.format("%02d/%02d/%04d", day, month, year)
    }
}
