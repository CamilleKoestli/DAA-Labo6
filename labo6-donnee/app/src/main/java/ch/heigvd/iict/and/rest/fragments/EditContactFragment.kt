package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentEditContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.models.Status
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel

class EditContactFragment : Fragment() {

    private var _binding: FragmentEditContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ContactsViewModel
    private var contactId: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditContactBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ContactsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupération de l'ID du contact
        contactId = arguments?.getLong("contactId")
        Log.d("EditContactFragment", "Contact ID received: $contactId")

        if (contactId == null) {
            Log.e("EditContactFragment", "Contact ID is null")
            Toast.makeText(requireContext(), "Contact ID is missing!", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        // Charger les données du contact
        viewModel.getContactById(contactId!!).observe(viewLifecycleOwner) { contact ->
            if (contact != null) {
                Log.d("EditContactFragment", "Contact found: $contact")
                bindContactData(contact)
            } else {
                Log.e("EditContactFragment", "Contact not found for ID: $contactId")
                Toast.makeText(requireContext(), "Contact not found", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.saveButton.setOnClickListener {
            val updatedContact = getContactFromForm()
            if (updatedContact != null) {
                updatedContact.id = contactId
                updatedContact.status = Status.UPDATED
                viewModel.updateContact(updatedContact)
                Toast.makeText(requireContext(), "Contact updated successfully", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.deleteButton.setOnClickListener {
            contactId?.let {
                viewModel.softDeleteContactById(it)
                Toast.makeText(requireContext(), "Contact deleted", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        binding.cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun bindContactData(contact: Contact) {
        binding.name.setText(contact.name)
        binding.firstname.setText(contact.firstname ?: "")
        binding.email.setText(contact.email ?: "")
        binding.birthday.setText(contact.birthday ?: "")
        binding.address.setText(contact.address ?: "")
        binding.zip.setText(contact.zip ?: "")
        binding.city.setText(contact.city ?: "")
        binding.phoneNumber.setText(contact.phoneNumber ?: "")
        when (contact.type) {
            PhoneType.HOME -> binding.radioGroupPhoneType.check(R.id.radio_home)
            PhoneType.OFFICE -> binding.radioGroupPhoneType.check(R.id.radio_office)
            PhoneType.MOBILE -> binding.radioGroupPhoneType.check(R.id.radio_mobile)
            PhoneType.FAX -> binding.radioGroupPhoneType.check(R.id.radio_fax)
            else -> binding.radioGroupPhoneType.clearCheck()
        }
    }

    private fun getContactFromForm(): Contact? {
        val name = binding.name.text.toString().trim()
        val phoneNumber = binding.phoneNumber.text.toString().trim()

        if (name.isEmpty() || phoneNumber.isEmpty()) return null

        return Contact(
            id = null,
            name = name,
            firstname = binding.firstname.text.toString().trim().ifEmpty { null },
            email = binding.email.text.toString().trim().ifEmpty { null },
            birthday = binding.birthday.text.toString().trim().ifEmpty { null },
            address = binding.address.text.toString().trim().ifEmpty { null },
            zip = binding.zip.text.toString().trim().ifEmpty { null },
            city = binding.city.text.toString().trim().ifEmpty { null },
            type = getSelectedPhoneType(),
            phoneNumber = phoneNumber,
            status = Status.UPDATED
        )
    }

    private fun getSelectedPhoneType(): PhoneType? {
        return when (binding.radioGroupPhoneType.checkedRadioButtonId) {
            R.id.radio_home -> PhoneType.HOME
            R.id.radio_office -> PhoneType.OFFICE
            R.id.radio_mobile -> PhoneType.MOBILE
            R.id.radio_fax -> PhoneType.FAX
            else -> null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
