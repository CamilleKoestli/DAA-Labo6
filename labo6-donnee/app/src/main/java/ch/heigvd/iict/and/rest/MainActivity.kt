package ch.heigvd.iict.and.rest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import ch.heigvd.iict.and.rest.databinding.ActivityMainBinding
import ch.heigvd.iict.and.rest.fragments.CreateContactFragment
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val contactsViewModel: ContactsViewModel by viewModels {
        ContactsViewModelFactory((application as ContactsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainFabNew.setOnClickListener {
            // Navigate to CreateContactFragment
            supportFragmentManager.commit{
                replace(R.id.main_content_fragment, CreateContactFragment())
                addToBackStack(null)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_main_synchronize -> {
                contactsViewModel.refresh()
                true
            }
            R.id.menu_main_populate -> {
                contactsViewModel.enroll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}