package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentListBinding
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

class ListFragment : Fragment() {

    private lateinit var binding : FragmentListBinding

    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(((requireActivity().application as ContactsApplication).repository))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactsAdapter(emptyList()) { _, _, _, id ->
            // we locate the contact to edit
            if(contactsViewModel.allContacts.value != null) {
                val selectedContact = contactsViewModel.allContacts.value!!.find { it.id == id }
                if(selectedContact != null) {
                    //FIXME - user clicks on selectedContact, we want to edit it
                    Toast.makeText(requireActivity(), "TODO - Edition de ${selectedContact.firstname} ${selectedContact.name}", Toast.LENGTH_SHORT).show()

                    // Set the selected contact in the ViewModel
                    selectedContact.id?.let { contactsViewModel.getContactById(it) }

                    // Navigate to the ContactEditFragment
                    navigateToContactEditFragment()
                }
            }
        }
        binding.listRecycler.adapter = adapter
        binding.listRecycler.layoutManager = LinearLayoutManager(requireContext())

        contactsViewModel.allContacts.observe(viewLifecycleOwner) { updatedContacts ->
            adapter.contacts = updatedContacts
            // we display an "empty view" when adapter contains no contact
            if(updatedContacts.isEmpty()) {
                binding.listRecycler.visibility = View.GONE
                binding.listContentEmpty.visibility = View.VISIBLE
            }
            else {
                binding.listContentEmpty.visibility = View.GONE
                binding.listRecycler.visibility = View.VISIBLE
            }
        }

        // Handle the FloatingActionButton to create a new contact
        /*
        binding.fab.setOnClickListener {
            // Clear the selected contact in the ViewModel for creating a new contact
            contactsViewModel.selectContact(null)

            // Navigate to the ContactEditFragment
            navigateToContactEditFragment()
        }*/

    }

    private fun navigateToContactEditFragment() {
        requireActivity().supportFragmentManager.commit {
            replace(R.id.main_content_fragment, EditContactFragment())
            addToBackStack(null) // Add the transaction to the back stack
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ListFragment()

        private val TAG = ListFragment::class.java.simpleName
    }

}