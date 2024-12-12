package ch.heigvd.iict.and.rest.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType

class ContactsAdapter(contacts : List<Contact>, private val clickListener: OnItemClickListener) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    var contacts : List<Contact> = contacts
    set(value) {
        val diffCallBack = ContactsDiffCallBack(contacts, value)
        val diffItem = DiffUtil.calculateDiff(diffCallBack)
        field = value
        diffItem.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactToDisplay = contacts[position]
        holder.bind(contactToDisplay, position)
    }

    override fun getItemCount() = contacts.size

    override fun getItemViewType(position: Int) = 0

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val image = view.findViewById<ImageView>(R.id.contact_image)
        private val name = view.findViewById<TextView>(R.id.contact_name)
        private val phonenumber = view.findViewById<TextView>(R.id.contact_phonenumber)
        private val type = view.findViewById<ImageView>(R.id.contact_phonenumber_type)

        fun bind(contact : Contact, position: Int) {
            view.setOnClickListener {
                clickListener.onItemClick(null, view, position, contact.id!!)
            }
            name.text = "${contact.name} ${contact.firstname}"
            phonenumber.text = "${contact.phoneNumber}"

            //FIXME color may depend on item sync status
            val colRes = android.R.color.holo_green_dark
            image.setColorFilter(ContextCompat.getColor(image.context, colRes))

            when(contact.type) {
                PhoneType.HOME -> type.setImageResource(R.drawable.phone)
                PhoneType.OFFICE -> type.setImageResource(R.drawable.office)
                PhoneType.MOBILE -> type.setImageResource(R.drawable.cellphone)
                PhoneType.FAX -> type.setImageResource(R.drawable.fax)
                else -> type.setImageResource(android.R.color.transparent)
            }

        }
    }
}

class ContactsDiffCallBack(private val oldList: List<Contact>, private val newList : List<Contact>) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldContact = oldList[oldItemPosition]
        val newContact = newList[newItemPosition]

        // FIXME - you may have to change the comparaison if the data class Contact change
        return  oldContact.name == newContact.name &&
                oldContact.firstname == newContact.firstname &&
                oldContact.birthday == newContact.birthday &&
                oldContact.email == newContact.email &&
                oldContact.address == newContact.address &&
                oldContact.zip == newContact.zip &&
                oldContact.city == newContact.city &&
                oldContact.type == newContact.type &&
                oldContact.phoneNumber == newContact.phoneNumber
    }

}