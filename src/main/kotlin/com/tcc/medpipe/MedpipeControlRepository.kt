package com.tcc.medpipe

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MedpipeControlRepository : JpaRepository<MedpipeControl, Long>