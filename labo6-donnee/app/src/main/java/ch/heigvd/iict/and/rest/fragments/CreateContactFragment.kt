package ch.heigvd.iict.and.rest.fragments

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
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel

class CreateContactFragment : Fragment() {

    private var _binding: FragmentCreateContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactsViewModel

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

        // Save button click logic
        binding.buttonSave.setOnClickListener {
            val name = binding.editName.text.toString()
            val firstname = binding.editFirstname.text.toString()
            val email = binding.editEmail.text.toString()
            val address = binding.editAddress.text.toString()
            val zip = binding.editZip.text.toString()
            val city = binding.editCity.text.toString()
            val phoneType = getSelectedPhoneType()
            val phoneNumber = binding.editPhone.text.toString()

            // Validate input
            // TODO check
            if (name.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Phone Number are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a new contact
            val newContact = Contact(
                id = null, // Let Room generate the ID
                name = name,
                firstname = if (firstname.isNotEmpty()) firstname else null,
                birthday = null,
                email = if (email.isNotEmpty()) email else null,
                address = if (address.isNotEmpty()) address else null,
                zip = if (zip.isNotEmpty()) zip else null,
                city = if (city.isNotEmpty()) city else null,
                type = phoneType,
                phoneNumber = if (phoneNumber.isNotEmpty()) phoneNumber else null,
                syncStatus = false // Default to unsynchronized
            )

            // Save the contact via ViewModel
            viewModel.insert(newContact)

            // Navigate back or close
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Cancel button logic
        binding.buttonCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    /**
     * Helper function to get the selected phone type from the RadioGroup
     */
    private fun getSelectedPhoneType(): PhoneType? {
        return when (binding.radioGroupPhoneType.checkedRadioButtonId) {
            R.id.radio_home -> PhoneType.HOME
            R.id.radio_mobile -> PhoneType.MOBILE
            R.id.radio_office -> PhoneType.OFFICE
            R.id.radio_fax -> PhoneType.FAX
            else -> null // No selection
        }
    }
}
