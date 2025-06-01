package net.domisafonov.propiotiempo.data.error

sealed class TimeError : PtError() {
    class LaterThanNow : TimeError()
    class IntersectingIntervals : TimeError()
}
