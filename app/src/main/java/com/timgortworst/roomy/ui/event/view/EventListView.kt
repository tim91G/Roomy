package com.timgortworst.roomy.ui.event.view

import com.timgortworst.roomy.model.Event

interface EventListView  {
    fun presentAddedEvent(agendaEvent: Event)
    fun presentEditedEvent(agendaEvent: Event)
    fun presentDeletedEvent(agendaEvent: Event)
    fun presentErrorView()
    fun presentEmptyView(isVisible: Boolean)
    fun setLoadingView(isLoading: Boolean)
}