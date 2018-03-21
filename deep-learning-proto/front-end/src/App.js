import React, { Component } from 'react';
import Images from './Images'
import Sidebar from './Sidebar'
import Dialog from './Dialog'

class App extends Component {
  state = {
    data: null,
    selectedImagesCount: 0,
    selectedSet: "",
    catSelected: false,
    isCat: false,
    show: false,
    clickedCats:{}
  }
  componentDidMount() {
    fetch('http://localhost:8080/api/datasets')
      .then(res => res.ok ? res.json() : console.error(res.status))
      .then(data => {
        this.setState({ data })
      })
  }
  handleDatasetClick(selectedSet, event) {
    this.setState({
      selectedImagesCount: this.state.data.find(set => set.name === selectedSet).imageCount,
      selectedSet,
      clickedCats: {}
    })
  }
  handleImageClick(imageNumber) {
    console.log("clicked", imageNumber)
    this.setState({ catSelected: false })
    fetch(`http://localhost:8080/api/${this.state.selectedSet}/${imageNumber}/prediction`)
      .then(res => res.json())
      .then(data => {
        this.setState({ catSelected: true, show: true, clickedCats: {...this.state.clickedCats, [imageNumber]: data.predictedLabel}})
        data.predictedLabel === 0 ? this.setState({ isCat: false }) : this.setState({ isCat: true })

        // dismiss "popup" after timeout
        setTimeout(() => {
          this.setState({show: false })
        }, 800);
      })
  }
  render() {
    if (this.state.data) return (
      <div className="App">
        <Sidebar
          data={this.state.data}
          handleDatasetClick={this.handleDatasetClick.bind(this)}
          selected={this.state.selectedSet}
        />
        {this.state.selectedSet === ""
          ? ""
          : <Images
              clickedCats={this.state.clickedCats}
              handleImageClick={this.handleImageClick.bind(this)}
              imagesCount={this.state.selectedImagesCount}
              selectedSet={this.state.selectedSet} />}
      {
        this.state.show && <Dialog isCat={this.state.isCat} />
      }
      </div>
    )
    else return <h1>loading</h1>
  }
}

export default App;
