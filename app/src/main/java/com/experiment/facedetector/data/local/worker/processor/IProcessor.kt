package com.experiment.facedetector.data.local.worker.processor

interface IProcessor {
    suspend fun process()
}

interface ICameraProcessor : IProcessor
