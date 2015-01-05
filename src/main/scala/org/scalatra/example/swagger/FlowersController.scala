package org.scalatra.example.swagger

import org.scalatra._
import org.scalatra.swagger._
import scala.collection.mutable.ArrayBuffer

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class FlowersController (implicit val swagger: Swagger) extends ScalatraServlet with JacksonJsonSupport with SwaggerSupport {

  // Sets up automatic case class to JSON output serialization
  protected implicit val jsonFormats: Formats = DefaultFormats

  protected val applicationDescription = "The flowershop API. It exposes operations for browsing and searching lists of flowers, and retrieving single flowers."

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  val getFlowers =
    (apiOperation[List[Flower]]("getFlowers")
      summary "Show all flowers"
      notes "Shows all the flowers in the flower shop. You can search it too."
      parameter queryParam[Option[String]]("name").description("A name to search for"))


  /*
   * Retrieve a list of flowers
   */
  get("/", operation(getFlowers)){
    params.get("name") match {
      case Some(name) => FlowerData.all filter (_.name.toLowerCase contains name.toLowerCase())
      case None => FlowerData.all
    }
  }

  val findBySlug = 
    (apiOperation[Flower]("findBySlug")
      summary "Find by slug"
      parameters (
        pathParam[String]("slug").description("Slug of flower that needs to be fetched")
      ))


  /**
   * Find a flower using its slug.
   */
  get("/:slug", operation(findBySlug)) {
    FlowerData.all find (_.slug == params("slug")) match {
      case Some(b) => b
      case None => halt(404)
    }
  }

  val createFlower = 
    (apiOperation[Flower]("createFlower")
      summary "Create new Flower and add to list."
      parameters(bodyParam[Flower]("body").description("Flower to add.").required)
    )

  post("/create", operation(createFlower)) {
   val newFlower = parsedBody.extract[Flower]

   FlowerData.all.append(newFlower)
   Created(newFlower, Map("Location"->("/flowers/" + newFlower.slug)))
  }
}


// A Flower object to use as a faked-out data model
case class Flower(slug: String, name: String)

// An amazing datastore!
object FlowerData {

  /**
   * Some fake flowers data so we can simulate retrievals.
   */
  var all = ArrayBuffer(
      Flower("yellow-tulip", "Yellow Tulip"),
      Flower("red-rose", "Red Rose"),
      Flower("black-rose", "Black Rose"))
}
