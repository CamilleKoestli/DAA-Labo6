/**
 * Authors : Koestli Camille / Oliveira Vitoria
 * Description : This fragment displays a list of contacts using a RecyclerView. It listens to updates
 *               from the ViewModel and updates the list dynamically. Clicking on a contact navigates
 *               to the EditContactFragment for editing or deleting the contact.
 */

package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentListBinding
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

class ListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding
    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory((requireActivity().application as ContactsApplication).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactsAdapter(emptyList()) { _, _, _, id ->
            Log.d("ListFragment", "Contact clicked with ID: $id")

            // Verify contact exists
            val selectedContact = contactsViewModel.allContacts.value?.find { it.id == id }
            if (selectedContact != null) {
                // Passing ID to the EditContactFragment
                navigateToContactEditFragment(id)
            } else {
                Log.e("ListFragment", "Contact with ID $id not found!")
                Toast.makeText(requireContext(), "Contact not found!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.listRecycler.adapter = adapter
        binding.listRecycler.layoutManager = LinearLayoutManager(requireContext())

        contactsViewModel.allContacts.observe(viewLifecycleOwner) { updatedContacts ->
            adapter.contacts = updatedContacts
            if (updatedContacts.isEmpty()) {
                binding.listRecycler.visibility = View.GONE
                binding.listContentEmpty.visibility = View.VISIBLE
            } else {
                binding.listContentEmpty.visibility = View.GONE
                binding.listRecycler.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToContactEditFragment(contactId: Long) {
        val fragment = EditContactFragment().apply {
            arguments = Bundle().apply {
                putLong("contactId", contactId)
            }
        }
        requireActivity().supportFragmentManager.commit {
            replace(R.id.main_content_fragment, fragment)
            addToBackStack(null)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListFragment()
    }
}
