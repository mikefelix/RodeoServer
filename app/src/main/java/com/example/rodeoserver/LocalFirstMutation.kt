package com.example.rodeoserver

class LocalFirstMutation(
  private val local: () -> WorkResult,
  private val remote: () -> WorkResult,
) {
  fun write(): WorkResult {
    local()
    return remote()
  }
}