package TravelCompanionScala {
package model {

import javax.persistence._
import javax.validation.constraints._
import org.hibernate.validator.constraints._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 26.04.2010
 * Time: 11:26:12
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "pictures")
class Picture {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column
  @NotEmpty
  var name: String = ""

  @Column
  var description: String = ""

  @Basic(fetch = FetchType.LAZY)
  @Lob
  @NotNull
  var image: Array[Byte] = null

  @Basic(fetch = FetchType.LAZY)
  @Lob
  @NotNull
  var thumbnail: Array[Byte] = null

  @Column
  @NotEmpty
  var imageType: String = ""

  @ManyToOne
  @NotNull
  var owner: Member = null

  @ManyToOne
  var tour: Tour = null

  //  @ManyToOne
  //  var stage: Stage

  @ManyToOne
  var blogEntry: BlogEntry = null
}

}
}