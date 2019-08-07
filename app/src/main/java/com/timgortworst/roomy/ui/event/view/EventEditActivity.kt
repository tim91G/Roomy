package com.timgortworst.roomy.ui.event.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.timgortworst.roomy.R
import com.timgortworst.roomy.domain.NotificationWorkerBuilder
import com.timgortworst.roomy.model.Category
import com.timgortworst.roomy.model.Event
import com.timgortworst.roomy.model.EventMetaData
import com.timgortworst.roomy.model.User
import com.timgortworst.roomy.ui.BaseActivity
import com.timgortworst.roomy.ui.event.adapter.SpinnerTaskAdapter
import com.timgortworst.roomy.ui.event.adapter.SpinnerUserAdapter
import com.timgortworst.roomy.ui.event.presenter.EventEditPresenter
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_edit_event.*
import java.util.*
import javax.inject.Inject


class EventEditActivity : BaseActivity(), EventEditView, DatePickerDialog.OnDateSetListener {
    private var event: Event? = null
    private lateinit var spinnerAdapterTasks: SpinnerTaskAdapter
    private lateinit var spinnerAdapterUsers: SpinnerUserAdapter
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var notificationWorkerBuilder: NotificationWorkerBuilder
    private var calendar = Calendar.getInstance()

    @Inject
    lateinit var presenter: EventEditPresenter

    companion object {
        const val INTENT_EXTRA_EDIT_EVENT = "INTENT_EXTRA_EDIT_EVENT"

        fun start(context: AppCompatActivity) {
            val intent = Intent(context, EventEditActivity::class.java)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up, R.anim.stay)
        }

        fun start(context: AppCompatActivity, agendaEvent: Event) {
            val intent = Intent(context, EventEditActivity::class.java)
            intent.putExtra(INTENT_EXTRA_EDIT_EVENT, agendaEvent)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up, R.anim.stay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        event = intent.getParcelableExtra(INTENT_EXTRA_EDIT_EVENT)

        supportActionBar?.apply {
            title = "Nieuw agenda item"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        event?.let {
            supportActionBar?.title = "Edit  ${it.eventCategory.name}"
            agenda_item_date_input.setText(it.eventMetaData.repeatStartDate.toString())
        }

        notificationWorkerBuilder = NotificationWorkerBuilder(this)

        presenter.getCategories()
        presenter.getUsers()
        setupEventRepeatSpinner()
        setupCalenderDialog()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        agenda_item_date_input.setOnClickListener {
            datePickerDialog.show()
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
        }

        event_repeat_checkbox.setOnCheckedChangeListener { _: CompoundButton, ischecked: Boolean ->
            if (ischecked) {
                event_repeat_view.visibility = View.VISIBLE
            } else {
                event_repeat_view.visibility = View.GONE
            }
        }
    }

    private fun setupEventRepeatSpinner() {
        spinner_repeat.adapter = ArrayAdapter<EventMetaData.RepeatingInterval>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                EventMetaData.RepeatingInterval.values().dropWhile { it == EventMetaData.RepeatingInterval.SINGLE_EVENT }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_edit_done -> {
                val eventMetaData = if (event_repeat_checkbox.isChecked) {
                    val repeatInterval = (spinner_repeat.selectedItem as EventMetaData.RepeatingInterval)
                    EventMetaData(repeatStartDate = calendar.timeInMillis, repeatInterval = repeatInterval)
                } else {
                    EventMetaData(repeatStartDate = calendar.timeInMillis, repeatInterval = EventMetaData.RepeatingInterval.SINGLE_EVENT)
                }

                presenter.createOrUpdateEvent(
                        event?.eventId,
                        (spinner_categories.selectedItem as Category),
                        (spinner_users.selectedItem as User),
                        eventMetaData
                )

                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        overridePendingTransition(R.anim.stay, R.anim.slide_down)
    }

    override fun presentUserList(users: MutableList<User>) {
        spinnerAdapterUsers = SpinnerUserAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                users
        )
        spinner_users.adapter = spinnerAdapterUsers
    }

    override fun presentCategoryList(tasks: MutableList<Category>) {
        spinnerAdapterTasks = SpinnerTaskAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                tasks
        )
        spinner_categories.adapter = spinnerAdapterTasks
    }

    private fun setupCalenderDialog() {
        datePickerDialog = DatePickerDialog(
                this, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        presenter.formatDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//        calendar.set(Calendar.HOUR_OF_DAY, 12)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.MILLISECOND, 0)

        presenter.formatDate(year, month, dayOfMonth)
    }

    override fun presentFormattedDate(formattedDayOfMonth: String, formattedMonth: String?, formattedYear: String) {
        agenda_item_date_input.setText("$formattedDayOfMonth $formattedMonth $formattedYear")
    }

    override fun enqueueOneTimeNotification(eventId: String,
                                            eventMetaData: EventMetaData,
                                            categoryName: String,
                                            userName: String) {
        notificationWorkerBuilder.enqueueOneTimeNotification(
                eventId,
                eventMetaData,
                categoryName,
                userName)
    }

    override fun enqueuePeriodicNotification(eventId: String,
                                             eventMetaData: EventMetaData,
                                             categoryName: String,
                                             userName: String) {
        notificationWorkerBuilder.enqueuePeriodicNotification(
                eventId,
                eventMetaData,
                categoryName,
                userName)
    }
}
