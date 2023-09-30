package com.tcc.medpipe.domain.repository

import com.tcc.medpipe.domain.model.MedpipeControl
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MedpipeControlRepository : JpaRepository<MedpipeControl, Long>