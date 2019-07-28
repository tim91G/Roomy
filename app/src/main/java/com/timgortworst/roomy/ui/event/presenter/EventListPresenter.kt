package com.timgortworst.roomy.ui.event.presenter

import android.widget.Filter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.DocumentChange
import com.timgortworst.roomy.model.Event
import com.timgortworst.roomy.model.EventMetaData
import com.timgortworst.roomy.repository.BaseResponse
import com.timgortworst.roomy.repository.EventRepository
import com.timgortworst.roomy.repository.UserRepository
import com.timgortworst.roomy.ui.event.view.EventListView
import com.timgortworst.roomy.utils.CoroutineLifecycleScope
import com.timgortworst.roomy.utils.isTimeStampInPast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class EventListPresenter @Inject constructor(
        private val view: EventListView,
        private val agendaRepository: EventRepository,
        private val userRepository: UserRepository
) : BaseResponse(), DefaultLifecycleObserver {
    private val scope = CoroutineLifecycleScope(Dispatchers.Main)

    init {
        if (view is LifecycleOwner) {
            view.lifecycle.addObserver(scope)
        }
    }

    fun detachEventListener() {
        agendaRepository.detachEventListener()
    }

    fun listenToEvents() = scope.launch {
        agendaRepository.listenToEventsForHousehold(
                userRepository.getHouseholdIdForUser(),
                this@EventListPresenter)
    }

    fun filterMe(filter: Filter) {
        filter.filter(userRepository.getCurrentUserId())
    }

    fun markEventAsCompleted(event: Event) = scope.launch {
        if (event.eventMetaData.repeatInterval == EventMetaData.RepeatingInterval.SINGLE_EVENT) {
            agendaRepository.deleteEvent(event.eventId)
            return@launch
        }

        val nextOccurance = calcNextOccurance(event)
        updateEventMetaData(event, nextOccurance)
    }

    private fun calcNextOccurance(event: Event): Long {
        return if (event.eventMetaData.repeatStartDate.isTimeStampInPast()) {
            System.currentTimeMillis() + (event.eventMetaData.repeatInterval.interval * 1000)
        } else {
            event.eventMetaData.repeatStartDate + (event.eventMetaData.repeatInterval.interval * 1000)
        }
    }

    private suspend fun updateEventMetaData(event: Event, nextOccurrence: Long) {
        val eventMetaData = EventMetaData(
                repeatStartDate = nextOccurrence,
                repeatInterval = event.eventMetaData.repeatInterval
        )

        event.eventMetaData = eventMetaData

        // reset done to false
        agendaRepository.updateEvent(event.eventId, eventMetaData = eventMetaData)
    }

    fun deleteEvent(event: Event) = scope.launch {
        agendaRepository.deleteEvent(event.eventId)
    }

    override fun renderSuccessfulState(dc: List<DocumentChange>?) {
        view.setLoadingView(false)

        dc?.let {
            if (it.isEmpty()) { view.presentEmptyView() }

            for (docChange in it) {
                val event = docChange.document.toObject(Event::class.java)
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
//                    if (event.eventMetaData.repeatStartDate.isTimeStampInPast()) {
//                        // event is in het verleden
//                        // todo send reminder
//                    }
                        view.presentAddedEvent(event)
                    }
                    DocumentChange.Type.MODIFIED -> view.presentEditedEvent(event)
                    DocumentChange.Type.REMOVED -> {
                        if (it.size == 1) { view.presentEmptyView() }
                        view.presentDeletedEvent(event)
                    }
                }
            }
        }
    }

    override fun renderLoadingState() {
        view.setLoadingView(true)
    }

    override fun renderUnsuccessfulState(throwable: Throwable) {
        view.setLoadingView(false)
        view.presentErrorView()
    }
}
