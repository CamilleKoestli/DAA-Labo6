package ch.heigvd.iict.and.rest.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ch.heigvd.iict.and.rest.MainActivity
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentEditContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel

class EditContactFragment : Fragment(R.layout.fragment_create_contact) {

    private var _binding: FragmentEditContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactsViewModel
    private var contactId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditContactBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ContactsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupération des arguments (ID du contact)
        contactId = arguments?.getLong("contactId")

        // Charger les données du contact
        if (contactId != null) {
            viewModel.getContactById(contactId!!).observe(viewLifecycleOwner) { contact ->
                if (contact != null) {
                    bindContactData(contact)
                } else {
                    Toast.makeText(requireContext(), "Contact not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp() // Retour si le contact n'existe pas
                }
            }
        }

        // Bouton Save (mise à jour des données)
        binding.saveButton.setOnClickListener {
            val updatedContact = getContactFromForm()
            if (updatedContact != null) {
                updatedContact.id = contactId
                viewModel.updateContact(updatedContact)
                Toast.makeText(requireContext(), "Contact updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton Delete (suppression du contact)
        binding.deleteButton.setOnClickListener {
            contactId?.let {
                viewModel.deleteContactById(it)
                Toast.makeText(requireContext(), "Contact deleted", Toast.LENGTH_SHORT).show()

                // Démarrer MainActivity avec une Intent
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

                // Fermer l'activité ou fragment actuel
                requireActivity().finish()
            }
        }

        // Bouton Cancel (annulation)
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun bindContactData(contact: Contact) {
        binding.name.setText(contact.name)
        binding.firstname.setText(contact.firstname)
        binding.email.setText(contact.email)
        binding.birthday.setText(contact.birthday)
        binding.address.setText(contact.address)
        binding.zip.setText(contact.zip)
        binding.city.setText(contact.city)
        binding.phoneNumber.setText(contact.phoneNumber)
        when (contact.type) {
            PhoneType.HOME -> binding.radioGroupPhoneType.check(R.id.radio_home)
            PhoneType.OFFICE -> binding.radioGroupPhoneType.check(R.id.radio_office)
            PhoneType.MOBILE -> binding.radioGroupPhoneType.check(R.id.radio_mobile)
            PhoneType.FAX -> binding.radioGroupPhoneType.check(R.id.radio_fax)
            else -> binding.radioGroupPhoneType.clearCheck() // Aucun bouton sélectionné
        }
    }

    private fun getContactFromForm(): Contact? {
        val name = binding.name.text.toString().trim()
        val phoneNumber = binding.phoneNumber.text.toString().trim()

        // Validation des champs requis
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
            syncStatus = false
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
        _binding = null // Éviter les fuites de mémoire
    }
}
